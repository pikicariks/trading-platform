package com.tradingplatform.order_service.service;

import com.tradingplatform.order_service.client.MarketDataServiceClient;
import com.tradingplatform.order_service.client.WalletServiceClient;
import com.tradingplatform.order_service.dto.CreateOrderRequestDto;
import com.tradingplatform.order_service.dto.OrderResponseDto;
import com.tradingplatform.order_service.dto.OrderSummaryResponseDto;
import com.tradingplatform.order_service.event.OrderEvent;
import com.tradingplatform.order_service.event.OrderEventProducer;
import com.tradingplatform.order_service.exception.InsufficientBalanceException;
import com.tradingplatform.order_service.exception.InvalidOrderException;
import com.tradingplatform.order_service.exception.OrderNotFoundException;
import com.tradingplatform.order_service.model.Order;
import com.tradingplatform.order_service.model.OrderStatus;
import com.tradingplatform.order_service.model.OrderType;
import com.tradingplatform.order_service.repository.OrderRepository;
import feign.FeignException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private WalletServiceClient walletServiceClient;

    @Autowired
    private MarketDataServiceClient marketDataServiceClient;

    @Autowired
    private OrderEventProducer orderEventProducer;

    @Value("${order.commission.rate:0.001}")
    private BigDecimal commissionRate;

    @Value("${order.limits.max-quantity:10000}")
    private Integer maxQuantity;

    @Value("${order.limits.min-quantity:1}")
    private Integer minQuantity;

    @Transactional
    @Override
    public OrderResponseDto createOrder(CreateOrderRequestDto request) {
        log.info("Creating order for user {}: {} {} shares of {}",
                request.getUserId(), request.getOrderType(), request.getQuantity(), request.getSymbol());

        validateOrderRequest(request);

        BigDecimal currentPrice = getCurrentStockPrice(request.getSymbol());

        BigDecimal totalAmount = calculateTotalAmount(request.getQuantity(), currentPrice);
        BigDecimal commission = calculateCommission(totalAmount);
        BigDecimal totalCost = totalAmount.add(commission);

        Order order = createPendingOrder(request, currentPrice, totalAmount, commission);
        Order savedOrder = orderRepository.save(order);

        try {
            processOrder(savedOrder, totalCost);
        } catch (Exception e) {
            log.error("Failed to process order {}: {}", savedOrder.getId(), e.getMessage());
            savedOrder.setStatus(OrderStatus.FAILED);
            savedOrder.setFailureReason(e.getMessage());
            orderRepository.save(savedOrder);
        }

        return mapToOrderResponse(savedOrder);
    }

    @Override
    public OrderResponseDto getOrderById(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        return mapToOrderResponse(order);
    }

    @Override
    public List<OrderResponseDto> getUserOrders(Long userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::mapToOrderResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Page<OrderResponseDto> getUserOrdersPaged(Long userId, Pageable pageable) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(this::mapToOrderResponse);
    }

    @Override
    public List<OrderResponseDto> getUserOrdersByStatus(Long userId, OrderStatus status) {
        return orderRepository.findByUserIdAndStatus(userId, status)
                .stream()
                .map(this::mapToOrderResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public OrderResponseDto cancelOrder(Long orderId, Long userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        if (!order.getUserId().equals(userId)) {
            throw new InvalidOrderException("You can only cancel your own orders");
        }

        if (order.getStatus() != OrderStatus.PENDING && order.getStatus() != OrderStatus.VALIDATING) {
            throw new InvalidOrderException("Can only cancel pending orders");
        }

        order.setStatus(OrderStatus.CANCELLED);
        Order saved = orderRepository.save(order);

        log.info("Order {} cancelled by user {}", orderId, userId);

        return mapToOrderResponse(saved);
    }

    @Override
    public OrderSummaryResponseDto getUserOrderSummary(Long userId) {
        long totalOrders = orderRepository.countByUserId(userId);
        long executedOrders = orderRepository.countByUserIdAndStatus(userId, OrderStatus.EXECUTED);
        long pendingOrders = orderRepository.countByUserIdAndStatus(userId, OrderStatus.PENDING);
        long failedOrders = orderRepository.countByUserIdAndStatus(userId, OrderStatus.FAILED);

        BigDecimal totalBuyAmount = orderRepository.getTotalAmountByUserAndType(userId, OrderType.BUY);
        BigDecimal totalSellAmount = orderRepository.getTotalAmountByUserAndType(userId, OrderType.SELL);

        if (totalBuyAmount == null) totalBuyAmount = BigDecimal.ZERO;
        if (totalSellAmount == null) totalSellAmount = BigDecimal.ZERO;

        List<Order> executedOrdersList = orderRepository.findByUserIdAndStatus(userId, OrderStatus.EXECUTED);
        BigDecimal totalCommission = executedOrdersList.stream()
                .map(Order::getCommission)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new OrderSummaryResponseDto(
                totalOrders,
                executedOrders,
                pendingOrders,
                failedOrders,
                totalBuyAmount,
                totalSellAmount,
                totalCommission
        );
    }

    private void processOrder(Order order, BigDecimal totalCost) {
        order.setStatus(OrderStatus.VALIDATING);
        orderRepository.save(order);

        if (order.getOrderType() == OrderType.BUY) {
            processBuyOrder(order, totalCost);
        } else {
            processSellOrder(order);
        }
    }

    private void processBuyOrder(Order order, BigDecimal totalCost) {
        log.info("Processing BUY order {} for user {}", order.getId(), order.getUserId());

        try {
            WalletServiceClient.BalanceResponse balance = walletServiceClient.getBalance(order.getUserId());

            if (balance.balance.compareTo(totalCost) < 0) {
                throw new InsufficientBalanceException(totalCost, balance.balance);
            }

            order.setStatus(OrderStatus.EXECUTING);
            orderRepository.save(order);

            WalletServiceClient.TransactionRequest deductRequest =
                    new WalletServiceClient.TransactionRequest(
                            totalCost,
                            String.format("Buy %d shares of %s at $%.2f",
                                    order.getQuantity(), order.getSymbol(), order.getPricePerShare()),
                            "ORDER-" + order.getId()
                    );

            walletServiceClient.deductForPurchase(order.getUserId(), deductRequest);

            order.setStatus(OrderStatus.EXECUTED);
            order.setExecutedAt(LocalDateTime.now());
            orderRepository.save(order);

            log.info("BUY order {} executed successfully", order.getId());

            OrderEvent event = OrderEvent.orderExecuted(
                    order.getId(),
                    order.getUserId(),
                    order.getSymbol(),
                    order.getOrderType(),
                    order.getQuantity(),
                    order.getPricePerShare(),
                    order.getTotalAmount()
            );
            orderEventProducer.publishOrderEvent(event);

        } catch (FeignException e) {
            log.error("Error processing order {}: {}", order.getId(), e.getMessage());
            order.setStatus(OrderStatus.FAILED);
            order.setFailureReason("Service communication error: " + e.getMessage());
            orderRepository.save(order);
            throw new InvalidOrderException("Failed to process order: " + e.getMessage());
        }
    }

    private void processSellOrder(Order order) {
        log.info("Processing SELL order {} for user {}", order.getId(), order.getUserId());

        try {
            // For now we'll assume user has shares (Portfolio Service will validate later)
            // In real implementation, check Portfolio Service first

            order.setStatus(OrderStatus.EXECUTING);
            orderRepository.save(order);

            BigDecimal saleAmount = order.getTotalAmount().subtract(order.getCommission());

            WalletServiceClient.TransactionRequest creditRequest =
                    new WalletServiceClient.TransactionRequest(
                            saleAmount,
                            String.format("Sell %d shares of %s at $%.2f",
                                    order.getQuantity(), order.getSymbol(), order.getPricePerShare()),
                            "ORDER-" + order.getId()
                    );

            walletServiceClient.creditFromSale(order.getUserId(), creditRequest);

            order.setStatus(OrderStatus.EXECUTED);
            order.setExecutedAt(LocalDateTime.now());
            orderRepository.save(order);

            log.info("SELL order {} executed successfully", order.getId());

            OrderEvent event = OrderEvent.orderExecuted(
                    order.getId(),
                    order.getUserId(),
                    order.getSymbol(),
                    order.getOrderType(),
                    order.getQuantity(),
                    order.getPricePerShare(),
                    order.getTotalAmount()
            );
            orderEventProducer.publishOrderEvent(event);

        } catch (FeignException e) {
            log.error("Error processing sell order {}: {}", order.getId(), e.getMessage());
            order.setStatus(OrderStatus.FAILED);
            order.setFailureReason("Service communication error: " + e.getMessage());
            orderRepository.save(order);
            throw new InvalidOrderException("Failed to process sell order: " + e.getMessage());
        }
    }

    private void validateOrderRequest(CreateOrderRequestDto request) {
        if (request.getQuantity() < minQuantity || request.getQuantity() > maxQuantity) {
            throw new InvalidOrderException(
                    String.format("Quantity must be between %d and %d", minQuantity, maxQuantity));
        }

        if (request.getSymbol() == null || request.getSymbol().trim().isEmpty()) {
            throw new InvalidOrderException("Symbol cannot be empty");
        }
    }

    private BigDecimal getCurrentStockPrice(String symbol) {
        try {
            BigDecimal price = marketDataServiceClient.getCurrentPrice(symbol.toUpperCase());

            if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
                throw new InvalidOrderException("Invalid stock price for symbol: " + symbol);
            }

            return price;
        } catch (FeignException e) {
            log.error("Failed to get price for {}: {}", symbol, e.getMessage());
            throw new InvalidOrderException("Stock not found or market data unavailable: " + symbol);
        }
    }

    private BigDecimal calculateTotalAmount(Integer quantity, BigDecimal pricePerShare) {
        return pricePerShare.multiply(new BigDecimal(quantity))
                .setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateCommission(BigDecimal totalAmount) {
        return totalAmount.multiply(commissionRate)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private Order createPendingOrder(CreateOrderRequestDto request, BigDecimal currentPrice,
                                     BigDecimal totalAmount, BigDecimal commission) {
        Order order = new Order();
        order.setUserId(request.getUserId());
        order.setSymbol(request.getSymbol().toUpperCase());
        order.setOrderType(request.getOrderType());
        order.setQuantity(request.getQuantity());
        order.setPricePerShare(currentPrice);
        order.setTotalAmount(totalAmount);
        order.setCommission(commission);
        order.setNotes(request.getNotes());
        order.setStatus(OrderStatus.PENDING);
        return order;
    }

    private OrderResponseDto mapToOrderResponse(Order order) {
        return new OrderResponseDto(
                order.getId(),
                order.getUserId(),
                order.getSymbol(),
                order.getOrderType(),
                order.getStatus(),
                order.getQuantity(),
                order.getPricePerShare(),
                order.getTotalAmount(),
                order.getCommission(),
                order.getNotes(),
                order.getFailureReason(),
                order.getCreatedAt(),
                order.getExecutedAt()
        );
    }
}
