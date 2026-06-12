'use client';

import { useEffect, useState } from 'react';
import { rulesService } from '@/lib/api/rules';
import { Rule } from '@/types/api';
import RuleEditModal from '@/components/RuleEditModal';
import { usePermissions } from '@/hooks/usePermissions';

export default function RulesPage() {
  const { canEdit } = usePermissions();

  const [rules, setRules] = useState<Rule[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [editingRule, setEditingRule] = useState<Rule | null>(null);
  const [showCreateModal, setShowCreateModal] = useState(false);

  useEffect(() => {
    loadRules();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const loadRules = async () => {
    try {
      setLoading(true);
      const response = await rulesService.getAll({ size: 100 });
      setRules(response.content);
      setError(null);
    } catch (err: any) {
      setError(err.message || 'Failed to load rules');
    } finally {
      setLoading(false);
    }
  };

  const handleToggle = async (rule: Rule) => {
    try {
      if (rule.enabled) {
        await rulesService.disable(rule.id);
      } else {
        await rulesService.enable(rule.id);
      }
      await loadRules();
    } catch (err: any) {
      setError(`Failed to ${rule.enabled ? 'disable' : 'enable'} rule: ${err.message}`);
    }
  };

  const handleEdit = (rule: Rule) => {
    setEditingRule(rule);
  };

  const handleCreate = () => {
    setShowCreateModal(true);
  };

  const handleSaveEdit = async (id: number, updates: Partial<Rule>) => {
    try {
      await rulesService.update(id, updates);
      await loadRules();
      setEditingRule(null);
    } catch (err: any) {
      setError(`Failed to update rule: ${err.message}`);
    }
  };

  const handleCreateRule = async (ruleData: Partial<Rule>) => {
    try {
      await rulesService.create(ruleData);
      await loadRules();
      setShowCreateModal(false);
    } catch (err: any) {
      setError(`Failed to create rule: ${err.message}`);
    }
  };

  const handleDelete = async (rule: Rule) => {
    if (!confirm(`Are you sure you want to delete rule "${rule.name}"?\n\nNote: Historical triggered transactions will be preserved for audit purposes.`)) {
      return;
    }
    try {
      await rulesService.delete(rule.id);
      await loadRules();
    } catch (err: any) {
      setError(`Failed to delete rule: ${err.message}`);
    }
  };

  const getRuleTypeColor = (type: string) => {
    const colors: Record<string, string> = {
      CUSTOMER_BLOCKLIST: 'badge-error',
      MERCHANT_BLOCKLIST: 'badge-error',
      AMOUNT_THRESHOLD: 'badge-blue',
      GEOGRAPHIC_ANOMALY: 'badge-warning',
      MERCHANT_RISK: 'badge-warning',
      AMOUNT_RANGE: 'badge-info',
      TIME_OF_DAY_ANOMALY: 'badge-purple',
      ROUND_AMOUNT: 'badge-secondary',
      CNP_HIGH_RISK: 'badge-warning',
      CURRENCY_MISMATCH: 'badge-info',
      CROSS_BORDER_HIGH_RISK: 'badge-error',
      LARGE_WITHDRAWAL: 'badge-blue',
    };
    return colors[type] || 'badge-neutral';
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="text-center">
          <div className="spinner-lg"></div>
          <p className="mt-4 text-cap-text-muted">Loading rules...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="card border-l-4 border-cap-red">
        <p className="text-cap-red font-medium">Error: {error}</p>
        <button
          onClick={loadRules}
          className="mt-2 text-sm text-cap-red hover:text-cap-red-600 underline"
        >
          Try again
        </button>
      </div>
    );
  }

  return (
    <div className="px-4 py-6 sm:px-0">
      <div className="sm:flex sm:items-center mb-6">
        <div className="sm:flex-auto">
          <h1 className="cap-page-title">Fraud Detection Rules</h1>
          <p className="mt-2 text-sm text-cap-text-muted">
            Manage fraud detection rules. Rules are evaluated in priority order (highest first).
            {!canEdit && <span className="text-cap-orange ml-2">(Read-only access)</span>}
          </p>
        </div>
        {canEdit && (
          <div className="mt-4 sm:mt-0 sm:ml-16 sm:flex-none">
            <button
              type="button"
              className="btn-primary"
              onClick={handleCreate}
            >
              Create Rule
            </button>
          </div>
        )}
      </div>

      <div className="space-y-4">
        {rules.map((rule) => (
          <div key={rule.id} className="card hover:shadow-card-hover transition-all duration-200">
            <div className="p-6">
              {/* Header Row: Name + Status Badge */}
              <div className="flex items-start justify-between mb-4">
                <div className="flex-1">
                  <div className="flex items-center gap-3 mb-2">
                    <h3 className="text-lg font-bold text-cap-deep-blue">
                      {rule.name.replace(/\s*\(Disabled\)\s*$/i, '').trim()}
                    </h3>
                    <span className={rule.enabled ? 'badge-success' : 'badge-neutral'}>
                      {rule.enabled ? 'ENABLED' : 'DISABLED'}
                    </span>
                  </div>
                  <div className="flex items-center gap-2">
                    <span className={getRuleTypeColor(rule.ruleType)}>
                      {rule.ruleType.replace(/_/g, ' ')}
                    </span>
                    <span className="text-cap-text-muted text-sm">•</span>
                    <span className="text-sm text-cap-text-muted">
                      Priority: <span className="font-semibold text-cap-deep-blue">{rule.priority}</span>
                    </span>
                  </div>
                </div>

                {/* Action Buttons */}
                {canEdit && (
                  <div className="flex items-center gap-2 ml-4">
                    <button
                      onClick={() => handleToggle(rule)}
                      className={rule.enabled ? 'btn-secondary' : 'btn-success'}
                    >
                      {rule.enabled ? 'Disable' : 'Enable'}
                    </button>
                    <button
                      onClick={() => handleEdit(rule)}
                      className="btn-outline"
                    >
                      Edit
                    </button>
                    <button
                      onClick={() => handleDelete(rule)}
                      className="btn-error"
                    >
                      Delete
                    </button>
                  </div>
                )}
              </div>

              {/* Rule Parameters */}
              <div className="flex flex-wrap items-center gap-4 mb-3 text-sm">
                {rule.thresholdAmount && (
                  <div className="flex items-center gap-1">
                    <span className="text-cap-text-muted">Threshold:</span>
                    <span className="font-semibold text-cap-text">{rule.thresholdAmount.toLocaleString()}</span>
                  </div>
                )}
                {rule.thresholdCount && (
                  <div className="flex items-center gap-1">
                    <span className="text-cap-text-muted">Count:</span>
                    <span className="font-semibold text-cap-text">{rule.thresholdCount}</span>
                  </div>
                )}
                {rule.timeWindowMinutes && (
                  <div className="flex items-center gap-1">
                    <span className="text-cap-text-muted">Window:</span>
                    <span className="font-semibold text-cap-text">{rule.timeWindowMinutes} min</span>
                  </div>
                )}
              </div>

              {/* Description */}
              {rule.description && (
                <p className="text-sm text-cap-text leading-relaxed">
                  {rule.description}
                </p>
              )}
            </div>
          </div>
        ))}
      </div>

      {rules.length === 0 && (
        <div className="card text-center py-16">
          <div className="max-w-md mx-auto">
            <p className="text-lg text-cap-text-muted mb-2">No rules found</p>
            <p className="text-sm text-cap-text-muted">Get started by creating your first fraud detection rule.</p>
          </div>
        </div>
      )}

      {/* Edit Modal */}
      {editingRule && (
        <RuleEditModal
          rule={editingRule}
          onClose={() => setEditingRule(null)}
          onSave={handleSaveEdit}
        />
      )}

      {/* Create Modal */}
      {showCreateModal && (
        <RuleEditModal
          onClose={() => setShowCreateModal(false)}
          onSave={handleSaveEdit}
          onCreate={handleCreateRule}
        />
      )}
    </div>
  );
}
