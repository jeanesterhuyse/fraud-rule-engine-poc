// Blocklist types

export interface BlockedCustomer {
  customerId: string;
  blockedAt: string;
  blockedBy?: string;
  reason: string;
  expiresAt?: string;
}

export interface BlockedMerchant {
  merchantName: string;
  blockedAt: string;
  blockedBy?: string;
  reason: string;
  expiresAt?: string;
}

export interface BlockCustomerRequest {
  customerId: string;
  reason: string;
  blockedBy?: string;
  expiresAt?: string;
}

export interface BlockMerchantRequest {
  merchantName: string;
  reason: string;
  blockedBy?: string;
  expiresAt?: string;
}
