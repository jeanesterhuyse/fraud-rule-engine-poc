'use client';

import { useEffect, useState } from 'react';
import { blocklistApi } from '@/lib/api/blocklists';
import { BlockedCustomer, BlockedMerchant, BlockCustomerRequest, BlockMerchantRequest } from '@/types/blocklist';

export default function BlocklistsPage() {
  const [activeTab, setActiveTab] = useState<'customers' | 'merchants'>('customers');
  const [customers, setCustomers] = useState<BlockedCustomer[]>([]);
  const [merchants, setMerchants] = useState<BlockedMerchant[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [showAddModal, setShowAddModal] = useState(false);

  useEffect(() => {
    loadData();
  }, [activeTab]);

  const loadData = async () => {
    try {
      setLoading(true);
      setError(null);
      if (activeTab === 'customers') {
        const data = await blocklistApi.getBlockedCustomers();
        setCustomers(data);
      } else {
        const data = await blocklistApi.getBlockedMerchants();
        setMerchants(data);
      }
    } catch (err: any) {
      setError(err.message || 'Failed to load blocklists');
    } finally {
      setLoading(false);
    }
  };

  const handleUnblockCustomer = async (customerId: string) => {
    if (!confirm(`Are you sure you want to unblock customer ${customerId}?`)) return;

    try {
      await blocklistApi.unblockCustomer(customerId);
      await loadData();
    } catch (err: any) {
      setError(`Failed to unblock customer: ${err.message}`);
    }
  };

  const handleUnblockMerchant = async (merchantName: string) => {
    if (!confirm(`Are you sure you want to unblock merchant "${merchantName}"?`)) return;

    try {
      await blocklistApi.unblockMerchant(merchantName);
      await loadData();
    } catch (err: any) {
      setError(`Failed to unblock merchant: ${err.message}`);
    }
  };

  const handleAddBlock = async (data: BlockCustomerRequest | BlockMerchantRequest) => {
    try {
      if (activeTab === 'customers') {
        await blocklistApi.blockCustomer(data as BlockCustomerRequest);
      } else {
        await blocklistApi.blockMerchant(data as BlockMerchantRequest);
      }
      setShowAddModal(false);
      await loadData();
    } catch (err: any) {
      setError(`Failed to add block: ${err.message}`);
    }
  };

  const isExpired = (expiresAt?: string) => {
    if (!expiresAt) return false;
    return new Date(expiresAt) < new Date();
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-2xl font-bold text-cap-deep-blue">Blocklists</h1>
          <p className="text-cap-text-muted mt-1">
            Manage blocked customers and merchants for instant fraud prevention
          </p>
        </div>
        <button
          onClick={() => setShowAddModal(true)}
          className="btn-primary"
        >
          + Add Block
        </button>
      </div>

      {/* Tabs */}
      <div className="card">
        <div className="flex border-b border-cap-grey-300">
          <button
            onClick={() => setActiveTab('customers')}
            className={`px-6 py-3 font-medium transition-colors ${
              activeTab === 'customers'
                ? 'text-cap-blue border-b-2 border-cap-blue'
                : 'text-cap-text-muted hover:text-cap-text'
            }`}
          >
            Blocked Customers ({customers.length})
          </button>
          <button
            onClick={() => setActiveTab('merchants')}
            className={`px-6 py-3 font-medium transition-colors ${
              activeTab === 'merchants'
                ? 'text-cap-blue border-b-2 border-cap-blue'
                : 'text-cap-text-muted hover:text-cap-text'
            }`}
          >
            Blocked Merchants ({merchants.length})
          </button>
        </div>

        {/* Error Alert */}
        {error && (
          <div className="m-4 p-4 bg-cap-red-50 border-l-4 border-cap-red text-cap-red rounded">
            {error}
          </div>
        )}

        {/* Loading State */}
        {loading && (
          <div className="flex items-center justify-center py-12">
            <div className="spinner-lg"></div>
          </div>
        )}

        {/* Customers Table */}
        {!loading && activeTab === 'customers' && (
          <div className="overflow-x-auto">
            {customers.length === 0 ? (
              <div className="text-center py-12 text-cap-text-muted">
                No blocked customers
              </div>
            ) : (
              <table className="min-w-full divide-y divide-cap-grey-300">
                <thead className="bg-cap-grey-100">
                  <tr>
                    <th className="px-4 py-3 text-left text-xs font-medium text-cap-text-muted uppercase tracking-wider w-48">
                      Customer ID
                    </th>
                    <th className="px-4 py-3 text-left text-xs font-medium text-cap-text-muted uppercase tracking-wider">
                      Reason
                    </th>
                    <th className="px-4 py-3 text-left text-xs font-medium text-cap-text-muted uppercase tracking-wider w-44">
                      Blocked At
                    </th>
                    <th className="px-4 py-3 text-left text-xs font-medium text-cap-text-muted uppercase tracking-wider w-32">
                      Blocked By
                    </th>
                    <th className="px-4 py-3 text-left text-xs font-medium text-cap-text-muted uppercase tracking-wider w-36">
                      Expires At
                    </th>
                    <th className="px-4 py-3 text-left text-xs font-medium text-cap-text-muted uppercase tracking-wider w-24">
                      Status
                    </th>
                    <th className="px-4 py-3 text-left text-xs font-medium text-cap-text-muted uppercase tracking-wider w-24">
                      Actions
                    </th>
                  </tr>
                </thead>
                <tbody className="bg-white divide-y divide-cap-grey-200">
                  {customers.map((customer) => (
                    <tr key={customer.customerId} className={isExpired(customer.expiresAt) ? 'opacity-50' : ''}>
                      <td className="px-4 py-3 whitespace-nowrap font-mono text-sm">
                        {customer.customerId}
                      </td>
                      <td className="px-4 py-3 text-sm">
                        <div className="max-w-md">{customer.reason}</div>
                      </td>
                      <td className="px-4 py-3 whitespace-nowrap text-sm text-cap-text-muted">
                        {new Date(customer.blockedAt).toLocaleDateString()} <br />
                        <span className="text-xs">{new Date(customer.blockedAt).toLocaleTimeString()}</span>
                      </td>
                      <td className="px-4 py-3 whitespace-nowrap text-sm">
                        {customer.blockedBy || '-'}
                      </td>
                      <td className="px-4 py-3 whitespace-nowrap text-sm text-cap-text-muted">
                        {customer.expiresAt ? (
                          <>
                            {new Date(customer.expiresAt).toLocaleDateString()} <br />
                            <span className="text-xs">{new Date(customer.expiresAt).toLocaleTimeString()}</span>
                          </>
                        ) : (
                          'Never'
                        )}
                      </td>
                      <td className="px-4 py-3 whitespace-nowrap">
                        {isExpired(customer.expiresAt) ? (
                          <span className="badge-neutral">Expired</span>
                        ) : (
                          <span className="badge-error">Active</span>
                        )}
                      </td>
                      <td className="px-4 py-3 whitespace-nowrap text-sm">
                        <button
                          onClick={() => handleUnblockCustomer(customer.customerId)}
                          className="text-cap-blue hover:text-cap-blue-600 font-medium"
                        >
                          Unblock
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>
        )}

        {/* Merchants Table */}
        {!loading && activeTab === 'merchants' && (
          <div className="overflow-x-auto">
            {merchants.length === 0 ? (
              <div className="text-center py-12 text-cap-text-muted">
                No blocked merchants
              </div>
            ) : (
              <table className="min-w-full divide-y divide-cap-grey-300">
                <thead className="bg-cap-grey-100">
                  <tr>
                    <th className="px-4 py-3 text-left text-xs font-medium text-cap-text-muted uppercase tracking-wider w-56">
                      Merchant Name
                    </th>
                    <th className="px-4 py-3 text-left text-xs font-medium text-cap-text-muted uppercase tracking-wider">
                      Reason
                    </th>
                    <th className="px-4 py-3 text-left text-xs font-medium text-cap-text-muted uppercase tracking-wider w-44">
                      Blocked At
                    </th>
                    <th className="px-4 py-3 text-left text-xs font-medium text-cap-text-muted uppercase tracking-wider w-32">
                      Blocked By
                    </th>
                    <th className="px-4 py-3 text-left text-xs font-medium text-cap-text-muted uppercase tracking-wider w-36">
                      Expires At
                    </th>
                    <th className="px-4 py-3 text-left text-xs font-medium text-cap-text-muted uppercase tracking-wider w-24">
                      Status
                    </th>
                    <th className="px-4 py-3 text-left text-xs font-medium text-cap-text-muted uppercase tracking-wider w-24">
                      Actions
                    </th>
                  </tr>
                </thead>
                <tbody className="bg-white divide-y divide-cap-grey-200">
                  {merchants.map((merchant) => (
                    <tr key={merchant.merchantName} className={isExpired(merchant.expiresAt) ? 'opacity-50' : ''}>
                      <td className="px-4 py-3 text-sm font-medium">
                        {merchant.merchantName}
                      </td>
                      <td className="px-4 py-3 text-sm">
                        <div className="max-w-md">{merchant.reason}</div>
                      </td>
                      <td className="px-4 py-3 whitespace-nowrap text-sm text-cap-text-muted">
                        {new Date(merchant.blockedAt).toLocaleDateString()} <br />
                        <span className="text-xs">{new Date(merchant.blockedAt).toLocaleTimeString()}</span>
                      </td>
                      <td className="px-4 py-3 whitespace-nowrap text-sm">
                        {merchant.blockedBy || '-'}
                      </td>
                      <td className="px-4 py-3 whitespace-nowrap text-sm text-cap-text-muted">
                        {merchant.expiresAt ? (
                          <>
                            {new Date(merchant.expiresAt).toLocaleDateString()} <br />
                            <span className="text-xs">{new Date(merchant.expiresAt).toLocaleTimeString()}</span>
                          </>
                        ) : (
                          'Never'
                        )}
                      </td>
                      <td className="px-4 py-3 whitespace-nowrap">
                        {isExpired(merchant.expiresAt) ? (
                          <span className="badge-neutral">Expired</span>
                        ) : (
                          <span className="badge-error">Active</span>
                        )}
                      </td>
                      <td className="px-4 py-3 whitespace-nowrap text-sm">
                        <button
                          onClick={() => handleUnblockMerchant(merchant.merchantName)}
                          className="text-cap-blue hover:text-cap-blue-600 font-medium"
                        >
                          Unblock
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>
        )}
      </div>

      {/* Add Block Modal */}
      {showAddModal && (
        <AddBlockModal
          type={activeTab}
          onClose={() => setShowAddModal(false)}
          onSave={handleAddBlock}
        />
      )}
    </div>
  );
}

interface AddBlockModalProps {
  type: 'customers' | 'merchants';
  onClose: () => void;
  onSave: (data: BlockCustomerRequest | BlockMerchantRequest) => Promise<void>;
}

function AddBlockModal({ type, onClose, onSave }: AddBlockModalProps) {
  const [formData, setFormData] = useState({
    id: '',
    reason: '',
    blockedBy: '',
    expiresAt: '',
  });
  const [saving, setSaving] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setSaving(true);

    try {
      const data = type === 'customers'
        ? {
            customerId: formData.id,
            reason: formData.reason,
            blockedBy: formData.blockedBy || undefined,
            expiresAt: formData.expiresAt || undefined,
          }
        : {
            merchantName: formData.id,
            reason: formData.reason,
            blockedBy: formData.blockedBy || undefined,
            expiresAt: formData.expiresAt || undefined,
          };

      await onSave(data);
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 overflow-y-auto" role="dialog" aria-modal="true">
      <div className="fixed inset-0 bg-cap-dark bg-opacity-50" onClick={onClose}></div>

      <div className="flex min-h-full items-center justify-center p-4">
        <div className="relative bg-cap-white rounded-xl shadow-2xl w-full max-w-lg">
          <div className="border-b border-cap-grey-300 px-6 py-4">
            <h2 className="text-xl font-bold text-cap-deep-blue">
              Block {type === 'customers' ? 'Customer' : 'Merchant'}
            </h2>
          </div>

          <form onSubmit={handleSubmit} className="p-6 space-y-4">
            <div>
              <label className="label">
                {type === 'customers' ? 'Customer ID' : 'Merchant Name'} <span className="text-cap-red">*</span>
              </label>
              <input
                type="text"
                value={formData.id}
                onChange={(e) => setFormData({ ...formData, id: e.target.value })}
                className="input w-full"
                required
                placeholder={type === 'customers' ? 'e.g., CUST-001' : 'e.g., Suspicious Merchant'}
              />
            </div>

            <div>
              <label className="label">
                Reason <span className="text-cap-red">*</span>
              </label>
              <textarea
                value={formData.reason}
                onChange={(e) => setFormData({ ...formData, reason: e.target.value })}
                className="input w-full"
                rows={3}
                required
                placeholder="Explain why this should be blocked..."
              />
            </div>

            <div>
              <label className="label">Blocked By (optional)</label>
              <input
                type="text"
                value={formData.blockedBy}
                onChange={(e) => setFormData({ ...formData, blockedBy: e.target.value })}
                className="input w-full"
                placeholder="e.g., fraud_analyst"
              />
            </div>

            <div>
              <label className="label">Expires At (optional)</label>
              <input
                type="datetime-local"
                value={formData.expiresAt}
                onChange={(e) => setFormData({ ...formData, expiresAt: e.target.value })}
                className="input w-full"
              />
              <p className="text-xs text-cap-text-muted mt-1">
                Leave empty for permanent block
              </p>
            </div>

            <div className="flex justify-end gap-3 pt-4">
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
                {saving ? 'Blocking...' : 'Block'}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
}
