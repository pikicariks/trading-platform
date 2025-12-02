export interface Order {
  id: number;
  userId: number;
  symbol: string;
  orderType: 'BUY' | 'SELL';
  status: 'PENDING' | 'VALIDATING' | 'EXECUTING' | 'EXECUTED' | 'FAILED' | 'CANCELLED';
  quantity: number;
  pricePerShare: number;
  totalAmount: number;
  commission: number;
  notes?: string;
  failureReason?: string;
  createdAt: Date;
  executedAt?: Date;
}

export interface CreateOrderRequest {
  userId: number;
  symbol: string;
  orderType: 'BUY' | 'SELL';
  quantity: number;
  notes?: string;
}
