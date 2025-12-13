import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject } from 'rxjs';
import { tap } from 'rxjs/operators';
import { environment } from '../../../environments/environment';

export interface Wallet {
  id: number;
  userId: number;
  balance: number;
  currency: string;
  isActive: boolean;
  createdAt: Date;
}

export interface BalanceResponse {
  userId: number;
  balance: number;
  currency: string;
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

export interface DepositRequest {
  userId: number;
  amount: number;
  description?: string;
}

export interface WithdrawRequest {
  userId: number;
  amount: number;
  description?: string;
}

export interface TransactionResponse {
  id: number;
  walletId: number;
  transactionType: string;
  amount: number;
  balanceAfter: number;
  description: string;
  createdAt: Date;
}

@Injectable({
  providedIn: 'root'
})
export class WalletService {
  private apiUrl = `${environment.apiUrl}/wallet`;
  private balanceSubject = new BehaviorSubject<number>(0);
  public balance$ = this.balanceSubject.asObservable();

  constructor(private http: HttpClient) {}

  createWallet(userId: number, role: string): Observable<Wallet> {
    return this.http.post<Wallet>(`${this.apiUrl}/create`, { userId, role });
  }

  getBalance(userId: number): Observable<BalanceResponse> {
    return this.http.get<BalanceResponse>(`${this.apiUrl}/user/${userId}/balance`)
      .pipe(
        tap(response => {
          this.balanceSubject.next(response.balance);
        })
      );
  }

  getCurrentBalance(): number {
    return this.balanceSubject.value;
  }

  updateBalance(balance: number): void {
    this.balanceSubject.next(balance);
  }

    getWallet(userId: number): Observable<Wallet> {
    return this.http.get<Wallet>(`${this.apiUrl}/user/${userId}`);
  }

  deposit(userId: number, amount: number, description?: string): Observable<TransactionResponse> {
    return this.http.post<TransactionResponse>(`${this.apiUrl}/user/${userId}/deposit`, {
      userId,
      amount,
      description
    }).pipe(
      tap(response => {
        this.balanceSubject.next(response.balanceAfter);
      })
    );
  }

    withdraw(userId: number, amount: number, description?: string): Observable<TransactionResponse> {
    return this.http.post<TransactionResponse>(`${this.apiUrl}/user/${userId}/withdraw`, {
      userId,
      amount,
      description
    }).pipe(
      tap(response => {
        this.balanceSubject.next(response.balanceAfter);
      })
    );
  }

  getTransactions(userId: number): Observable<Transaction[]> {
    return this.http.get<Transaction[]>(`${this.apiUrl}/user/${userId}/transactions`);
  }
}