export interface User {
  id: number;
  username: string;
  email: string;
  role: string;
  isActive?: boolean;
  createdAt?: Date;
}

export interface LoginRequest {
  usernameOrEmail: string;
  password: string;
}

export interface RegisterRequest {
  username: string;
  email: string;
  password: string;
  role?: string;
}

export interface AuthResponse {
  token: string;
  type?: string;
  userId?: number;
  username: string;
  email: string;
  role: string;
}
