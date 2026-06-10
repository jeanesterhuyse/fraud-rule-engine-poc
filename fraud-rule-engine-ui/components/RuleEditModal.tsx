'use client';

import { useEffect, useState } from 'react';
import { Rule, RuleType } from '@/types/api';

interface RuleEditModalProps {
  rule?: Rule; // Optional - if not provided, we're creating a new rule
  onClose: () => void;
  onSave: (id: number, updates: Partial<Rule>) => Promise<void>;
  onCreate?: (ruleData: Partial<Rule>) => Promise<void>;
}

const RULE_TYPES: { value: RuleType; label: string; description: string }[] = [
  { value: 'CUSTOMER_BLOCKLIST', label: 'Customer Blocklist', description: 'Instantly blocks transactions from blocklisted customers (highest priority)' },
  { value: 'MERCHANT_BLOCKLIST', label: 'Merchant Blocklist', description: 'Instantly blocks transactions at blocklisted merchants' },
  { value: 'AMOUNT_THRESHOLD', label: 'Amount Threshold', description: 'Triggers on transactions exceeding a specific amount' },
  { value: 'GEOGRAPHIC_ANOMALY', label: 'Geographic Anomaly', description: 'Triggers on transactions from high-risk countries' },
  { value: 'MERCHANT_RISK', label: 'Merchant Risk', description: 'Triggers on high-risk merchant categories (e.g., gambling)' },
  { value: 'AMOUNT_RANGE', label: 'Amount Range', description: 'Triggers on transactions within a suspicious range (e.g., structuring)' },
  { value: 'TIME_OF_DAY_ANOMALY', label: 'Time of Day Anomaly', description: 'Triggers on transactions during unusual hours (e.g., 2-5 AM)' },
  { value: 'ROUND_AMOUNT', label: 'Round Amount', description: 'Triggers on large round-number amounts (card testing pattern)' },
  { value: 'CNP_HIGH_RISK', label: 'CNP High Risk', description: 'Card-not-present transactions at high-risk merchants (online fraud)' },
  { value: 'CURRENCY_MISMATCH', label: 'Currency Mismatch', description: 'Triggers on foreign currency transactions outside home country' },
  { value: 'CROSS_BORDER_HIGH_RISK', label: 'Cross-Border High Risk', description: 'Cross-border transactions to high-risk countries' },
  { value: 'LARGE_WITHDRAWAL', label: 'Large Withdrawal', description: 'Triggers on large ATM or cash withdrawals' },
];

export default function RuleEditModal({ rule, onClose, onSave, onCreate }: RuleEditModalProps) {
  const isCreateMode = !rule;

  const [formData, setFormData] = useState<Partial<Rule>>({
    name: rule?.name || '',
    description: rule?.description || '',
    ruleType: rule?.ruleType || 'AMOUNT_THRESHOLD',
    priority: rule?.priority || 100,
    thresholdAmount: rule?.thresholdAmount,
    thresholdCount: rule?.thresholdCount,
    timeWindowMinutes: rule?.timeWindowMinutes,
    merchantCategory: rule?.merchantCategory,
    countryCode: rule?.countryCode,
    minAmount: rule?.minAmount,
    maxAmount: rule?.maxAmount,
    // New fields
    startHour: rule?.startHour,
    endHour: rule?.endHour,
    minimumAmount: rule?.minimumAmount,
    roundToNearest: rule?.roundToNearest,
    customerHomeCountry: rule?.customerHomeCountry,
    customerHomeCurrency: rule?.customerHomeCurrency,
  });

  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // Close modal on Escape key
  useEffect(() => {
    const handleEscape = (e: KeyboardEvent) => {
      if (e.key === 'Escape') onClose();
    };
    window.addEventListener('keydown', handleEscape);
    return () => window.removeEventListener('keydown', handleEscape);
  }, [onClose]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setSaving(true);

    try {
      if (isCreateMode && onCreate) {
        await onCreate(formData);
      } else if (rule) {
        await onSave(rule.id, formData);
      }
      onClose();
    } catch (err: any) {
      setError(err.message || `Failed to ${isCreateMode ? 'create' : 'save'} rule`);
    } finally {
      setSaving(false);
    }
  };

  const handleChange = (field: keyof Rule, value: any) => {
    setFormData(prev => ({ ...prev, [field]: value }));
  };

  // Determine which fields to show based on rule type
  const showThresholdAmount = ['AMOUNT_THRESHOLD', 'LARGE_WITHDRAWAL'].includes(formData.ruleType!);
  const showAmountRange = formData.ruleType === 'AMOUNT_RANGE';
  const showCountryCode = ['GEOGRAPHIC_ANOMALY', 'CROSS_BORDER_HIGH_RISK'].includes(formData.ruleType!);
  const showMerchantCategory = ['MERCHANT_RISK', 'CNP_HIGH_RISK'].includes(formData.ruleType!);
  const showTimeOfDay = formData.ruleType === 'TIME_OF_DAY_ANOMALY';
  const showRoundAmount = formData.ruleType === 'ROUND_AMOUNT';
  const showCurrencyMismatch = formData.ruleType === 'CURRENCY_MISMATCH';
  const showCrossBorder = formData.ruleType === 'CROSS_BORDER_HIGH_RISK';
  const showNoConfig = ['CUSTOMER_BLOCKLIST', 'MERCHANT_BLOCKLIST'].includes(formData.ruleType!);

  return (
    <div className="fixed inset-0 z-50 overflow-y-auto" aria-labelledby="modal-title" role="dialog" aria-modal="true">
      {/* Backdrop */}
      <div className="fixed inset-0 bg-cap-dark bg-opacity-50 transition-opacity" onClick={onClose}></div>

      {/* Modal */}
      <div className="flex min-h-full items-center justify-center p-4">
        <div className="relative bg-cap-white rounded-xl shadow-2xl w-full max-w-2xl animate-fade-up">
          {/* Header */}
          <div className="border-b border-cap-grey-300 px-6 py-4">
            <div className="flex items-center justify-between">
              <h2 className="text-xl font-bold text-cap-deep-blue">
                {isCreateMode ? 'Create New Rule' : 'Edit Rule'}
              </h2>
              <button
                onClick={onClose}
                className="text-cap-text-muted hover:text-cap-text transition-colors"
              >
                <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                </svg>
              </button>
            </div>
          </div>

          {/* Form */}
          <form onSubmit={handleSubmit} className="px-6 py-6">
            {error && (
              <div className="mb-4 p-3 bg-cap-red/10 border border-cap-red/20 rounded text-cap-red text-sm">
                {error}
              </div>
            )}

            <div className="space-y-4 max-h-[60vh] overflow-y-auto pr-2">
              {/* Name */}
              <div>
                <label htmlFor="name" className="label">
                  Rule Name <span className="text-cap-red">*</span>
                </label>
                <input
                  type="text"
                  id="name"
                  value={formData.name || ''}
                  onChange={(e) => handleChange('name', e.target.value)}
                  className="input w-full"
                  required
                />
              </div>

              {/* Description */}
              <div>
                <label htmlFor="description" className="label">
                  Description
                </label>
                <textarea
                  id="description"
                  value={formData.description || ''}
                  onChange={(e) => handleChange('description', e.target.value)}
                  className="textarea w-full"
                  rows={3}
                  placeholder="Describe when this rule should trigger and why it's important..."
                />
              </div>

              {/* Rule Type Description */}
              {isCreateMode && (
                <div className="bg-cap-blue-50 border border-cap-blue-200 rounded p-3 text-sm text-cap-text">
                  <strong className="text-cap-deep-blue">Tip:</strong>{' '}
                  {RULE_TYPES.find(t => t.value === formData.ruleType)?.description}
                </div>
              )}

              <div className="grid grid-cols-2 gap-4">
                {/* Rule Type */}
                <div>
                  <label htmlFor="ruleType" className="label">
                    Rule Type <span className="text-cap-red">*</span>
                  </label>
                  <select
                    id="ruleType"
                    value={formData.ruleType || ''}
                    onChange={(e) => handleChange('ruleType', e.target.value)}
                    className="select w-full"
                    required
                  >
                    {RULE_TYPES.map(type => (
                      <option key={type.value} value={type.value}>
                        {type.label}
                      </option>
                    ))}
                  </select>
                </div>

                {/* Priority */}
                <div>
                  <label htmlFor="priority" className="label">
                    Priority <span className="text-cap-red">*</span>
                  </label>
                  <input
                    type="number"
                    id="priority"
                    value={formData.priority || 0}
                    onChange={(e) => handleChange('priority', parseInt(e.target.value))}
                    className="input w-full"
                    required
                    min="1"
                    max="1000"
                  />
                  <p className="text-xs text-cap-text-muted mt-1">Higher numbers = higher priority (evaluated first)</p>
                </div>
              </div>

              {/* Conditional Fields Based on Rule Type */}

              {/* No configuration needed for blocklist rules */}
              {showNoConfig && (
                <div className="bg-cap-blue-50 border border-cap-blue-200 rounded p-4 text-sm">
                  <p className="text-cap-text">
                    <strong className="text-cap-deep-blue">Note:</strong> This rule type checks against the blocklist tables.
                    No additional configuration is required. Manage blocklists in the Blocklists section.
                  </p>
                </div>
              )}

              {/* AMOUNT_THRESHOLD, LARGE_WITHDRAWAL */}
              {showThresholdAmount && (
                <div>
                  <label htmlFor="thresholdAmount" className="label">
                    Threshold Amount <span className="text-cap-red">*</span>
                  </label>
                  <input
                    type="number"
                    id="thresholdAmount"
                    value={formData.thresholdAmount || ''}
                    onChange={(e) => handleChange('thresholdAmount', parseFloat(e.target.value))}
                    className="input w-full"
                    step="0.01"
                    required
                    placeholder="e.g., 50000.00"
                  />
                  <p className="text-xs text-cap-text-muted mt-1">
                    {formData.ruleType === 'LARGE_WITHDRAWAL' ? 'Maximum withdrawal amount before triggering' : 'Minimum transaction amount to trigger'}
                  </p>
                </div>
              )}

              {/* AMOUNT_RANGE */}
              {showAmountRange && (
                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <label htmlFor="minAmount" className="label">
                      Min Amount <span className="text-cap-red">*</span>
                    </label>
                    <input
                      type="number"
                      id="minAmount"
                      value={formData.minAmount || ''}
                      onChange={(e) => handleChange('minAmount', parseFloat(e.target.value))}
                      className="input w-full"
                      step="0.01"
                      required
                      placeholder="e.g., 9000.00"
                    />
                  </div>
                  <div>
                    <label htmlFor="maxAmount" className="label">
                      Max Amount <span className="text-cap-red">*</span>
                    </label>
                    <input
                      type="number"
                      id="maxAmount"
                      value={formData.maxAmount || ''}
                      onChange={(e) => handleChange('maxAmount', parseFloat(e.target.value))}
                      className="input w-full"
                      step="0.01"
                      required
                      placeholder="e.g., 9999.99"
                    />
                  </div>
                </div>
              )}

              {/* GEOGRAPHIC_ANOMALY, CROSS_BORDER_HIGH_RISK */}
              {showCountryCode && (
                <div>
                  <label htmlFor="countryCode" className="label">
                    Country Code (ISO 3166-1) <span className="text-cap-red">*</span>
                  </label>
                  <input
                    type="text"
                    id="countryCode"
                    value={formData.countryCode || ''}
                    onChange={(e) => handleChange('countryCode', e.target.value.toUpperCase())}
                    className="input w-full"
                    maxLength={3}
                    required
                    placeholder="e.g., RUS, PRK, USA"
                  />
                  <p className="text-xs text-cap-text-muted mt-1">3-letter country code (alpha-3)</p>
                </div>
              )}

              {/* MERCHANT_RISK, CNP_HIGH_RISK */}
              {showMerchantCategory && (
                <div>
                  <label htmlFor="merchantCategory" className="label">
                    Merchant Category <span className="text-cap-red">*</span>
                  </label>
                  <input
                    type="text"
                    id="merchantCategory"
                    value={formData.merchantCategory || ''}
                    onChange={(e) => handleChange('merchantCategory', e.target.value)}
                    className="input w-full"
                    required
                    placeholder="e.g., GAMBLING, ELECTRONICS, JEWELRY"
                  />
                  <p className="text-xs text-cap-text-muted mt-1">
                    {formData.ruleType === 'CNP_HIGH_RISK' ? 'High-risk category for card-not-present transactions' : 'High-risk merchant category'}
                  </p>
                </div>
              )}

              {/* TIME_OF_DAY_ANOMALY */}
              {showTimeOfDay && (
                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <label htmlFor="startHour" className="label">
                      Start Hour (0-23) <span className="text-cap-red">*</span>
                    </label>
                    <input
                      type="number"
                      id="startHour"
                      value={formData.startHour ?? ''}
                      onChange={(e) => handleChange('startHour', parseInt(e.target.value))}
                      className="input w-full"
                      min="0"
                      max="23"
                      required
                      placeholder="e.g., 2"
                    />
                  </div>
                  <div>
                    <label htmlFor="endHour" className="label">
                      End Hour (0-23) <span className="text-cap-red">*</span>
                    </label>
                    <input
                      type="number"
                      id="endHour"
                      value={formData.endHour ?? ''}
                      onChange={(e) => handleChange('endHour', parseInt(e.target.value))}
                      className="input w-full"
                      min="0"
                      max="23"
                      required
                      placeholder="e.g., 5"
                    />
                  </div>
                </div>
              )}

              {/* ROUND_AMOUNT */}
              {showRoundAmount && (
                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <label htmlFor="minimumAmount" className="label">
                      Minimum Amount <span className="text-cap-red">*</span>
                    </label>
                    <input
                      type="number"
                      id="minimumAmount"
                      value={formData.minimumAmount || ''}
                      onChange={(e) => handleChange('minimumAmount', parseFloat(e.target.value))}
                      className="input w-full"
                      step="0.01"
                      required
                      placeholder="e.g., 1000.00"
                    />
                    <p className="text-xs text-cap-text-muted mt-1">Only check amounts above this</p>
                  </div>
                  <div>
                    <label htmlFor="roundToNearest" className="label">
                      Round To Nearest <span className="text-cap-red">*</span>
                    </label>
                    <select
                      id="roundToNearest"
                      value={formData.roundToNearest || ''}
                      onChange={(e) => handleChange('roundToNearest', parseInt(e.target.value))}
                      className="select w-full"
                      required
                    >
                      <option value="">Select...</option>
                      <option value="10">10</option>
                      <option value="50">50</option>
                      <option value="100">100</option>
                      <option value="500">500</option>
                      <option value="1000">1000</option>
                    </select>
                    <p className="text-xs text-cap-text-muted mt-1">Check if amount is multiple of this</p>
                  </div>
                </div>
              )}

              {/* CURRENCY_MISMATCH */}
              {showCurrencyMismatch && (
                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <label htmlFor="customerHomeCountry" className="label">
                      Customer Home Country <span className="text-cap-red">*</span>
                    </label>
                    <input
                      type="text"
                      id="customerHomeCountry"
                      value={formData.customerHomeCountry || ''}
                      onChange={(e) => handleChange('customerHomeCountry', e.target.value.toUpperCase())}
                      className="input w-full"
                      maxLength={3}
                      required
                      placeholder="e.g., ZAF"
                    />
                    <p className="text-xs text-cap-text-muted mt-1">3-letter country code</p>
                  </div>
                  <div>
                    <label htmlFor="customerHomeCurrency" className="label">
                      Customer Home Currency <span className="text-cap-red">*</span>
                    </label>
                    <input
                      type="text"
                      id="customerHomeCurrency"
                      value={formData.customerHomeCurrency || ''}
                      onChange={(e) => handleChange('customerHomeCurrency', e.target.value.toUpperCase())}
                      className="input w-full"
                      maxLength={3}
                      required
                      placeholder="e.g., ZAR"
                    />
                    <p className="text-xs text-cap-text-muted mt-1">3-letter currency code (ISO 4217)</p>
                  </div>
                </div>
              )}

              {/* CROSS_BORDER_HIGH_RISK */}
              {showCrossBorder && (
                <div>
                  <label htmlFor="customerHomeCountry" className="label">
                    Customer Home Country <span className="text-cap-red">*</span>
                  </label>
                  <input
                    type="text"
                    id="customerHomeCountry"
                    value={formData.customerHomeCountry || ''}
                    onChange={(e) => handleChange('customerHomeCountry', e.target.value.toUpperCase())}
                    className="input w-full"
                    maxLength={3}
                    required
                    placeholder="e.g., ZAF"
                  />
                  <p className="text-xs text-cap-text-muted mt-1">Customer's home country (3-letter code)</p>
                </div>
              )}
            </div>

            {/* Footer */}
            <div className="flex justify-end gap-3 mt-6 pt-4 border-t border-cap-grey-300">
              <button
                type="button"
                onClick={onClose}
                className="btn-secondary"
                disabled={saving}
              >
                Cancel
              </button>
              <button
                type="submit"
                className="btn-primary"
                disabled={saving}
              >
                {saving ? (
                  <span className="flex items-center gap-2">
                    <div className="spinner"></div>
                    {isCreateMode ? 'Creating...' : 'Saving...'}
                  </span>
                ) : (
                  isCreateMode ? 'Create Rule' : 'Save Changes'
                )}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
}
