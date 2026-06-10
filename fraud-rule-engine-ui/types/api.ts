// Authentication types
export interface LoginRequest {
  username: string;
  password: string;
}

export interface AuthResponse {
  token: string;
  username: string;
  roles: string[];
  expiresAt: string;
}

// Rule types
export interface Rule {
  id: number;
  name: string;
  description: string;
  ruleType: RuleType;
  enabled: boolean;
  priority: number;
  thresholdAmount?: number;
  thresholdCount?: number;
  timeWindowMinutes?: number;
  merchantCategory?: string;
  countryCode?: string;
  minAmount?: number;
  maxAmount?: number;
  createdAt: string;
  updatedAt: string;
  createdBy?: string;
  updatedBy?: string;
}

export type RuleType = 
  | 'AMOUNT_THRESHOLD'
  | 'VELOCITY'
  | 'GEOGRAPHIC_ANOMALY'
  | 'MERCHANT_RISK'
  | 'AMOUNT_RANGE'
  | 'RAPID_FIRE'
  | 'DORMANT_ACCOUNT';

// Triggered Transaction types
export interface TriggeredTransaction {
  id: number;
  ruleId: number;
  ruleName: string;
  ruleType: RuleType;
  transactionId: string;
  accountId: string;
  customerId: string;
  amount: number;
  currency: string;
  merchantName?: string;
  merchantCategory?: string;
  transactionType: string;
  transactionTimestamp: string;
  countryCode?: string;
  deviceId?: string;
  ipAddress?: string;
  cardLastFour?: string;
  matchReason: string;
  triggeredAt: string;
  riskScore?: number;
}

// Dashboard types
export interface DashboardSummary {
  totalTriggeredTransactions: number;
  totalActiveRules: number;
  totalTriggersLast24Hours: number;
  totalTriggersLast7Days: number;
  averageRiskScore: number;
  highestRiskScore: number;
  totalFlaggedAmount: number;
  currency: string;
}

// Paginated response
export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}
