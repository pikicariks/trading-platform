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

@Injectable({
  providedIn: 'root'
})
export class WalletService {
  private apiUrl = `${environment.apiUrl}/wallet`;
  private balanceSubject = new BehaviorSubject<number>(0);
  public balance$ = this.balanceSubject.asObservable();

  constructor(private http: HttpClient) {}

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
}