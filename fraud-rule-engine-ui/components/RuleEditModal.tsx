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
  { value: 'AMOUNT_THRESHOLD', label: 'Amount Threshold', description: 'Triggers on transactions exceeding a specific amount' },
  { value: 'VELOCITY', label: 'Velocity', description: 'Triggers on multiple transactions within a time window' },
  { value: 'GEOGRAPHIC_ANOMALY', label: 'Geographic Anomaly', description: 'Triggers on transactions from specific countries' },
  { value: 'MERCHANT_RISK', label: 'Merchant Risk', description: 'Triggers on high-risk merchant categories' },
  { value: 'AMOUNT_RANGE', label: 'Amount Range', description: 'Triggers on transactions within a specific amount range' },
  { value: 'RAPID_FIRE', label: 'Rapid Fire', description: 'Triggers on rapid succession of transactions' },
  { value: 'DORMANT_ACCOUNT', label: 'Dormant Account', description: 'Triggers on activity from dormant accounts' },
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
  const showThresholdAmount = ['AMOUNT_THRESHOLD', 'MERCHANT_RISK'].includes(formData.ruleType!);
  const showThresholdCount = ['VELOCITY', 'RAPID_FIRE'].includes(formData.ruleType!);
  const showTimeWindow = ['VELOCITY', 'RAPID_FIRE', 'DORMANT_ACCOUNT'].includes(formData.ruleType!);
  const showAmountRange = formData.ruleType === 'AMOUNT_RANGE';
  const showCountryCode = formData.ruleType === 'GEOGRAPHIC_ANOMALY';
  const showMerchantCategory = formData.ruleType === 'MERCHANT_RISK';

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
              {showThresholdAmount && (
                <div>
                  <label htmlFor="thresholdAmount" className="label">
                    Threshold Amount
                  </label>
                  <input
                    type="number"
                    id="thresholdAmount"
                    value={formData.thresholdAmount || ''}
                    onChange={(e) => handleChange('thresholdAmount', parseFloat(e.target.value))}
                    className="input w-full"
                    step="0.01"
                  />
                </div>
              )}

              {showThresholdCount && (
                <div>
                  <label htmlFor="thresholdCount" className="label">
                    Threshold Count
                  </label>
                  <input
                    type="number"
                    id="thresholdCount"
                    value={formData.thresholdCount || ''}
                    onChange={(e) => handleChange('thresholdCount', parseInt(e.target.value))}
                    className="input w-full"
                    min="1"
                  />
                </div>
              )}

              {showTimeWindow && (
                <div>
                  <label htmlFor="timeWindowMinutes" className="label">
                    Time Window (minutes)
                  </label>
                  <input
                    type="number"
                    id="timeWindowMinutes"
                    value={formData.timeWindowMinutes || ''}
                    onChange={(e) => handleChange('timeWindowMinutes', parseInt(e.target.value))}
                    className="input w-full"
                    min="1"
                  />
                </div>
              )}

              {showAmountRange && (
                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <label htmlFor="minAmount" className="label">
                      Min Amount
                    </label>
                    <input
                      type="number"
                      id="minAmount"
                      value={formData.minAmount || ''}
                      onChange={(e) => handleChange('minAmount', parseFloat(e.target.value))}
                      className="input w-full"
                      step="0.01"
                    />
                  </div>
                  <div>
                    <label htmlFor="maxAmount" className="label">
                      Max Amount
                    </label>
                    <input
                      type="number"
                      id="maxAmount"
                      value={formData.maxAmount || ''}
                      onChange={(e) => handleChange('maxAmount', parseFloat(e.target.value))}
                      className="input w-full"
                      step="0.01"
                    />
                  </div>
                </div>
              )}

              {showCountryCode && (
                <div>
                  <label htmlFor="countryCode" className="label">
                    Country Code
                  </label>
                  <input
                    type="text"
                    id="countryCode"
                    value={formData.countryCode || ''}
                    onChange={(e) => handleChange('countryCode', e.target.value.toUpperCase())}
                    className="input w-full"
                    maxLength={3}
                    placeholder="e.g., RUS, USA"
                  />
                </div>
              )}

              {showMerchantCategory && (
                <div>
                  <label htmlFor="merchantCategory" className="label">
                    Merchant Category
                  </label>
                  <input
                    type="text"
                    id="merchantCategory"
                    value={formData.merchantCategory || ''}
                    onChange={(e) => handleChange('merchantCategory', e.target.value)}
                    className="input w-full"
                  />
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
