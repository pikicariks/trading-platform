import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatChipsModule } from '@angular/material/chips';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { StockSearchComponent } from '../stock-search/stock-search.component';
import { StockCardComponent } from '../stock-card/stock-card.component';
import { MarketService, StockQuote, WatchlistItem } from '../../../core/service/market.service';
import { AuthService } from '../../../core/service/auth.service';

@Component({
  selector: 'app-market-page',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatChipsModule,
    MatSnackBarModule,
    StockSearchComponent,
    StockCardComponent
  ],
  templateUrl: './market-page.component.html',
  styleUrl: './market-page.component.scss'
})
export class MarketPageComponent implements OnInit {
  popularStocks: string[] = ['AAPL', 'MSFT', 'GOOGL', 'AMZN', 'TSLA', 'META'];
  popularQuotes: StockQuote[] = [];
  watchlistQuotes: StockQuote[] = [];
  searchResults: StockQuote[] = [];
  
  loadingPopular = true;
  loadingWatchlist = false;
  searchPerformed = false;

  constructor(
    private marketService: MarketService,
    private authService: AuthService,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.loadPopularStocks();
    this.loadWatchlist();
  }

  loadPopularStocks(): void {
    this.loadingPopular = true;
    let loaded = 0;

    this.popularStocks.forEach(symbol => {
      this.marketService.getQuote(symbol).subscribe({
        next: (quote) => {
          this.popularQuotes.push(quote);
          loaded++;
          if (loaded === this.popularStocks.length) {
            this.loadingPopular = false;
          }
        },
        error: (error) => {
          console.error(`Error loading ${symbol}:`, error);
          loaded++;
          if (loaded === this.popularStocks.length) {
            this.loadingPopular = false;
          }
        }
      });
    });
  }

  loadWatchlist(): void {
    const user = this.authService.getCurrentUser();
    if (!user) return;

    this.loadingWatchlist = true;
    
    this.marketService.getWatchlist(user.id).subscribe({
      next: (watchlist) => {
        if (watchlist.length === 0) {
          this.loadingWatchlist = false;
          return;
        }

        let loaded = 0;
        this.watchlistQuotes = [];

        watchlist.forEach(item => {
          this.marketService.getQuote(item.symbol).subscribe({
            next: (quote) => {
              this.watchlistQuotes.push(quote);
              loaded++;
              if (loaded === watchlist.length) {
                this.loadingWatchlist = false;
              }
            },
            error: (error) => {
              console.error(`Error loading watchlist ${item.symbol}:`, error);
              loaded++;
              if (loaded === watchlist.length) {
                this.loadingWatchlist = false;
              }
            }
          });
        });
      },
      error: (error) => {
        console.error('Error loading watchlist:', error);
        this.loadingWatchlist = false;
      }
    });
  }

  onSearch(symbol: string): void {
    this.searchPerformed = true;
    this.searchResults = [];

    this.marketService.getQuote(symbol).subscribe({
      next: (quote) => {
        this.searchResults = [quote];
      },
      error: (error) => {
        console.error('Search error:', error);
        this.snackBar.open(`Stock "${symbol}" not found`, 'Close', {
          duration: 3000,
          panelClass: ['error-snackbar']
        });
      }
    });
  }

  onToggleWatchlist(symbol: string): void {
    const user = this.authService.getCurrentUser();
    if (!user) return;

    const isInWatchlist = this.marketService.isInWatchlist(symbol);

    if (isInWatchlist) {
      this.marketService.removeFromWatchlist(user.id, symbol).subscribe({
        next: () => {
          this.snackBar.open(`${symbol} removed from watchlist`, 'Close', {
            duration: 2000
          });
          this.loadWatchlist();
        },
        error: (error) => {
          console.error('Error removing from watchlist:', error);
          this.snackBar.open('Failed to update watchlist', 'Close', {
            duration: 3000,
            panelClass: ['error-snackbar']
          });
        }
      });
    } else {
      this.marketService.addToWatchlist(user.id, symbol).subscribe({
        next: () => {
          this.snackBar.open(`${symbol} added to watchlist`, 'Close', {
            duration: 2000,
            panelClass: ['success-snackbar']
          });
          this.loadWatchlist();
        },
        error: (error) => {
          console.error('Error adding to watchlist:', error);
          this.snackBar.open('Failed to update watchlist', 'Close', {
            duration: 3000,
            panelClass: ['error-snackbar']
          });
        }
      });
    }
  }

  onTrade(symbol: string): void {
    // TODO: Open trade dialog
    this.snackBar.open('Trading dialog coming soon!', 'Close', { duration: 2000 });
  }
}