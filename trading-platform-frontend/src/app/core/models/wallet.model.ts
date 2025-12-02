export interface Wallet {
  id: number;
  userId: number;
  balance: number;
  currency: string;
  isActive: boolean;
  createdAt: Date;
}

export interface Transaction {
  id: number;
  walletId: number;
  transactionType: string;
  amount: number;
  balanceAfter: number;
  description: string;
  createdAt: Date;
}
