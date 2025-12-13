import { Component, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-deposit-dialog',
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
  templateUrl: './deposit-dialog.component.html',
  styleUrl: './deposit-dialog.component.scss'
})
export class DepositDialogComponent {
  depositForm: FormGroup;
  quickAmounts = [100, 500, 1000, 5000, 10000, 50000];

  constructor(
    private fb: FormBuilder,
    public dialogRef: MatDialogRef<DepositDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { currentBalance: number }
  ) {
    this.depositForm = this.fb.group({
      amount: ['', [Validators.required, Validators.min(1), Validators.max(1000000)]],
      description: ['']
    });
  }

  setQuickAmount(amount: number): void {
    this.depositForm.patchValue({ amount });
  }

  onSubmit(): void {
    if (this.depositForm.valid) {
      this.dialogRef.close(this.depositForm.value);
    }
  }

  onCancel(): void {
    this.dialogRef.close();
  }

  get amount() {
    return this.depositForm.get('amount');
  }
}