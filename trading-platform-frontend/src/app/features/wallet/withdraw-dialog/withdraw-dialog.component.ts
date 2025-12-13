import { Component, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-withdraw-dialog',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule
  ],
  templateUrl: './withdraw-dialog.component.html',
  styleUrl: './withdraw-dialog.component.scss'
})
export class WithdrawDialogComponent {
  withdrawForm: FormGroup;
  quickAmounts: number[] = [];

  constructor(
    private fb: FormBuilder,
    public dialogRef: MatDialogRef<WithdrawDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { currentBalance: number }
  ) {
    this.withdrawForm = this.fb.group({
      amount: ['', [
        Validators.required, 
        Validators.min(1), 
        Validators.max(data.currentBalance)
      ]],
      description: ['']
    });

    this.generateQuickAmounts();
  }

  generateQuickAmounts(): void {
    const balance = this.data.currentBalance;
    const amounts = [100, 500, 1000, 5000, 10000];
    
    this.quickAmounts = amounts
      .filter(amount => amount <= balance)
      .concat([balance])
      .slice(0, 6);
  }

  setQuickAmount(amount: number): void {
    this.withdrawForm.patchValue({ amount });
  }

  onSubmit(): void {
    if (this.withdrawForm.valid) {
      this.dialogRef.close(this.withdrawForm.value);
    }
  }

  onCancel(): void {
    this.dialogRef.close();
  }

  get amount() {
    return this.withdrawForm.get('amount');
  }
}