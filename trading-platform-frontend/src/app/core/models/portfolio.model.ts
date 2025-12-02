export interface Portfolio {
  id: number;
  userId: number;
  totalValue: number;
  cashBalance: number;
  investedAmount: number;
  totalProfitLoss: number;
  totalProfitLossPercent: number;
  holdings: Holding[];
}

export interface Holding {
  id: number;
  symbol: string;
  companyName: string;
  quantity: number;
  averagePrice: number;
  currentPrice: number;
  totalValue: number;
  totalInvested: number;
  profitLoss: number;
  profitLossPercent: number;
}
