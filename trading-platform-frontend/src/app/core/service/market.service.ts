import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject } from 'rxjs';
import { tap } from 'rxjs/operators';
import { environment } from '../../../environments/environment';

export interface StockQuote {
  symbol: string;
  companyName: string;
  price: number;
  change: number;
  changePercent: number;
  previousClose: number;
  open: number;
  dayHigh: number;
  dayLow: number;
  volume: number;
  lastUpdated: string;
}

export interface StockSearchResult {
  symbol: string;
  name: string;
  type: string;
  region: string;
  currency: string;
}

export interface CompanyDetails {
  symbol: string;
  companyName: string;
  exchange: string;
  sector: string;
  industry: string;
  marketCap: number;
  peRatio: number;
  dividendYield: number;
  week52High: number;
  week52Low: number;
  description: string;
}

export interface WatchlistItem {
  id: number;
  userId: number;
  symbol: string;
  addedAt: string;
}

@Injectable({
  providedIn: 'root'
})
export class MarketService {
  private apiUrl = `${environment.apiUrl}/market`;
  private watchlistSubject = new BehaviorSubject<WatchlistItem[]>([]);
  public watchlist$ = this.watchlistSubject.asObservable();

  constructor(private http: HttpClient) {}

  searchStocks(keywords: string): Observable<StockSearchResult[]> {
    return this.http.get<StockSearchResult[]>(`${this.apiUrl}/search`, {
      params: { keywords }
    });
  }

  getQuote(symbol: string): Observable<StockQuote> {
    return this.http.get<StockQuote>(`${this.apiUrl}/quote/${symbol.toUpperCase()}`);
  }

  getPrice(symbol: string): Observable<number> {
    return this.http.get<number>(`${this.apiUrl}/price/${symbol.toUpperCase()}`);
  }

  getCompanyDetails(symbol: string): Observable<CompanyDetails> {
    return this.http.get<CompanyDetails>(`${this.apiUrl}/company/${symbol.toUpperCase()}`);
  }

  getWatchlist(userId: number): Observable<WatchlistItem[]> {
    return this.http.get<WatchlistItem[]>(`${this.apiUrl}/watchlist/${userId}`)
      .pipe(
        tap(watchlist => this.watchlistSubject.next(watchlist))
      );
  }

  addToWatchlist(userId: number, symbol: string): Observable<WatchlistItem> {
    return this.http.post<WatchlistItem>(`${this.apiUrl}/watchlist`, {
      userId,
      symbol: symbol.toUpperCase()
    }).pipe(
      tap(() => this.getWatchlist(userId).subscribe())
    );
  }

  removeFromWatchlist(userId: number, symbol: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/watchlist/${userId}/${symbol.toUpperCase()}`)
      .pipe(
        tap(() => this.getWatchlist(userId).subscribe())
      );
  }

  isInWatchlist(symbol: string): boolean {
    return this.watchlistSubject.value.some(
      item => item.symbol.toUpperCase() === symbol.toUpperCase()
    );
  }
}