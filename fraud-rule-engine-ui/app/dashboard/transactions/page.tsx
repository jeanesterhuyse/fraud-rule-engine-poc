'use client';

import { useEffect, useState } from 'react';
import { transactionsService } from '@/lib/api/transactions';
import { TriggeredTransaction } from '@/types/api';

export default function TransactionsPage() {
  const [transactions, setTransactions] = useState<TriggeredTransaction[]>([]);
  const [allTransactions, setAllTransactions] = useState<TriggeredTransaction[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // Filter states
  const [startDate, setStartDate] = useState('');
  const [endDate, setEndDate] = useState('');
  const [selectedRuleType, setSelectedRuleType] = useState('');
  const [showFilters, setShowFilters] = useState(false);

  // Pagination states
  const [currentPage, setCurrentPage] = useState(1);
  const [itemsPerPage, setItemsPerPage] = useState(20);
  const [totalPages, setTotalPages] = useState(0);
  const [totalItems, setTotalItems] = useState(0);

  useEffect(() => {
    loadTransactions();
  }, []);

  const loadTransactions = async (reapplyFilters = false) => {
    try {
      setLoading(true);
      const response = await transactionsService.getAll({ size: 200 });
      setAllTransactions(response.content);

      if (reapplyFilters && (startDate || endDate || selectedRuleType)) {
        // Reapply existing filters to the new data
        let filtered = [...response.content];

        if (startDate) {
          const start = new Date(startDate);
          filtered = filtered.filter(txn => new Date(txn.triggeredAt) >= start);
        }
        if (endDate) {
          const end = new Date(endDate);
          end.setHours(23, 59, 59, 999);
          filtered = filtered.filter(txn => new Date(txn.triggeredAt) <= end);
        }
        if (selectedRuleType) {
          filtered = filtered.filter(txn => txn.ruleType === selectedRuleType);
        }

        setTransactions(filtered);
      } else {
        setTransactions(response.content);
      }

      setError(null);
    } catch (err: any) {
      setError(err.message || 'Failed to load transactions');
    } finally {
      setLoading(false);
    }
  };

  const handleRefresh = () => {
    loadTransactions(true);
  };

  const applyFilters = () => {
    let filtered = [...allTransactions];

    // Filter by date range
    if (startDate) {
      const start = new Date(startDate);
      filtered = filtered.filter(txn => new Date(txn.triggeredAt) >= start);
    }
    if (endDate) {
      const end = new Date(endDate);
      end.setHours(23, 59, 59, 999); // Include the entire end date
      filtered = filtered.filter(txn => new Date(txn.triggeredAt) <= end);
    }

    // Filter by rule type
    if (selectedRuleType) {
      filtered = filtered.filter(txn => txn.ruleType === selectedRuleType);
    }

    setTransactions(filtered);
    setTotalItems(filtered.length);
    setTotalPages(Math.ceil(filtered.length / itemsPerPage));
    setCurrentPage(1); // Reset to first page when filters change
  };

  const clearFilters = () => {
    setStartDate('');
    setEndDate('');
    setSelectedRuleType('');
    setTransactions(allTransactions);
    setTotalItems(allTransactions.length);
    setTotalPages(Math.ceil(allTransactions.length / itemsPerPage));
    setCurrentPage(1);
  };

  // Calculate paginated data
  const paginatedTransactions = transactions.slice(
    (currentPage - 1) * itemsPerPage,
    currentPage * itemsPerPage
  );

  const handlePageChange = (page: number) => {
    setCurrentPage(page);
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  const handleItemsPerPageChange = (newItemsPerPage: number) => {
    setItemsPerPage(newItemsPerPage);
    setTotalPages(Math.ceil(transactions.length / newItemsPerPage));
    setCurrentPage(1); // Reset to first page
  };

  // Update pagination when transactions change
  useEffect(() => {
    setTotalItems(transactions.length);
    setTotalPages(Math.ceil(transactions.length / itemsPerPage));
  }, [transactions, itemsPerPage]);

  const uniqueRuleTypes = Array.from(new Set(allTransactions.map(txn => txn.ruleType)));

  const getRiskScoreColor = (score?: number) => {
    if (!score) return 'bg-cap-grey-100 text-cap-text-muted';
    if (score >= 80) return 'bg-cap-red/10 text-cap-red border border-cap-red/20';
    if (score >= 60) return 'bg-cap-orange/10 text-cap-orange-600 border border-cap-orange/20';
    if (score >= 40) return 'bg-cap-yellow/10 text-cap-yellow-700 border border-cap-yellow/20';
    return 'bg-cap-green/10 text-cap-green border border-cap-green/20';
  };

  const getRuleTypeColor = (type: string) => {
    const colors: Record<string, string> = {
      AMOUNT_THRESHOLD: 'badge-blue',
      VELOCITY: 'badge-purple',
      GEOGRAPHIC_ANOMALY: 'badge-error',
      MERCHANT_RISK: 'badge-warning',
      AMOUNT_RANGE: 'badge-info',
      RAPID_FIRE: 'badge-secondary',
      DORMANT_ACCOUNT: 'badge-neutral',
    };
    return colors[type] || 'badge-neutral';
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleString();
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="text-center">
          <div className="spinner-lg"></div>
          <p className="mt-4 text-cap-text-muted">Loading transactions...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="card border-l-4 border-cap-red">
        <p className="text-cap-red font-medium">Error: {error}</p>
        <button
          onClick={loadTransactions}
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
          <h1 className="cap-page-title">Triggered Transactions</h1>
          <p className="mt-2 text-sm text-cap-text-muted">
            Transactions that matched fraud detection rules. Latest transactions shown first.
          </p>
        </div>
        <div className="mt-4 sm:mt-0 sm:ml-16 sm:flex-none flex gap-2">
          <button
            onClick={() => setShowFilters(!showFilters)}
            className="btn-outline"
          >
            {showFilters ? 'Hide Filters' : 'Show Filters'}
          </button>
          <button
            onClick={handleRefresh}
            className="btn-secondary"
            disabled={loading}
          >
            {loading ? (
              <span className="flex items-center gap-2">
                <div className="spinner"></div>
                Refreshing...
              </span>
            ) : (
              'Refresh'
            )}
          </button>
        </div>
      </div>

      {/* Filter Panel */}
      {showFilters && (
        <div className="card mb-6 animate-fade-up">
          <div className="p-6">
            <h3 className="cap-section-title mb-4">Filter Transactions</h3>
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
              {/* Start Date */}
              <div>
                <label htmlFor="startDate" className="label">
                  Start Date
                </label>
                <input
                  type="date"
                  id="startDate"
                  value={startDate}
                  onChange={(e) => setStartDate(e.target.value)}
                  className="input w-full"
                />
              </div>

              {/* End Date */}
              <div>
                <label htmlFor="endDate" className="label">
                  End Date
                </label>
                <input
                  type="date"
                  id="endDate"
                  value={endDate}
                  onChange={(e) => setEndDate(e.target.value)}
                  className="input w-full"
                />
              </div>

              {/* Rule Type */}
              <div>
                <label htmlFor="ruleType" className="label">
                  Rule Type
                </label>
                <select
                  id="ruleType"
                  value={selectedRuleType}
                  onChange={(e) => setSelectedRuleType(e.target.value)}
                  className="select w-full"
                >
                  <option value="">All Rule Types</option>
                  {uniqueRuleTypes.map((type) => (
                    <option key={type} value={type}>
                      {type.replace(/_/g, ' ')}
                    </option>
                  ))}
                </select>
              </div>
            </div>

            {/* Filter Actions */}
            <div className="flex gap-3 mt-6">
              <button
                onClick={applyFilters}
                className="btn-primary"
              >
                Apply Filters
              </button>
              <button
                onClick={clearFilters}
                className="btn-secondary"
              >
                Clear Filters
              </button>
              {(startDate || endDate || selectedRuleType) && (
                <span className="text-sm text-cap-text-muted self-center ml-2">
                  Showing {transactions.length} of {allTransactions.length} transactions
                </span>
              )}
            </div>
          </div>
        </div>
      )}

      <div className="card overflow-hidden">
        <div className="overflow-x-auto">
          <table className="min-w-full divide-y divide-cap-grey-200">
            <thead className="bg-cap-grey-50">
              <tr>
                <th className="px-6 py-3 text-left text-xs font-semibold text-cap-text-muted uppercase tracking-wider">
                  Transaction
                </th>
                <th className="px-6 py-3 text-left text-xs font-semibold text-cap-text-muted uppercase tracking-wider">
                  Rule
                </th>
                <th className="px-6 py-3 text-left text-xs font-semibold text-cap-text-muted uppercase tracking-wider">
                  Amount
                </th>
                <th className="px-6 py-3 text-left text-xs font-semibold text-cap-text-muted uppercase tracking-wider">
                  Risk Score
                </th>
                <th className="px-6 py-3 text-left text-xs font-semibold text-cap-text-muted uppercase tracking-wider">
                  Time
                </th>
                <th className="px-6 py-3 text-left text-xs font-semibold text-cap-text-muted uppercase tracking-wider">
                  Reason
                </th>
              </tr>
            </thead>
            <tbody className="bg-cap-white divide-y divide-cap-grey-200">
              {paginatedTransactions.map((txn) => (
                <tr key={txn.id} className="hover:bg-cap-grey-50 transition-colors duration-150">
                  <td className="px-6 py-4 whitespace-nowrap">
                    <div className="text-sm font-semibold text-cap-deep-blue">{txn.transactionId}</div>
                    <div className="text-sm text-cap-text-muted">Customer: {txn.customerId}</div>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <div className="text-sm text-cap-text font-medium mb-1">{txn.ruleName}</div>
                    <span className={getRuleTypeColor(txn.ruleType)}>
                      {txn.ruleType.replace(/_/g, ' ')}
                    </span>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <div className="text-sm text-cap-text font-semibold">{txn.currency} {txn.amount.toLocaleString()}</div>
                    {txn.merchantName && (
                      <div className="text-sm text-cap-text-muted">{txn.merchantName}</div>
                    )}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <span className={`px-3 py-1 inline-flex text-xs font-bold rounded-full ${getRiskScoreColor(txn.riskScore)}`}>
                      {txn.riskScore || 'N/A'}
                    </span>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-cap-text-muted">
                    {formatDate(txn.triggeredAt)}
                  </td>
                  <td className="px-6 py-4 text-sm text-cap-text-muted max-w-xs truncate">
                    {txn.matchReason}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {transactions.length === 0 && (
        <div className="card text-center py-12">
          <p className="text-cap-text-muted">No triggered transactions found</p>
        </div>
      )}

      {/* Pagination Controls */}
      {transactions.length > 0 && (
        <div className="mt-6 flex flex-col sm:flex-row items-center justify-between gap-4">
          {/* Items per page selector */}
          <div className="flex items-center gap-2">
            <label htmlFor="itemsPerPage" className="text-sm text-cap-text-muted">
              Show:
            </label>
            <select
              id="itemsPerPage"
              value={itemsPerPage}
              onChange={(e) => handleItemsPerPageChange(parseInt(e.target.value))}
              className="select text-sm"
            >
              <option value="10">10</option>
              <option value="20">20</option>
              <option value="50">50</option>
              <option value="100">100</option>
            </select>
            <span className="text-sm text-cap-text-muted">
              per page
            </span>
          </div>

          {/* Page info */}
          <div className="text-sm text-cap-text-muted font-medium">
            Showing {(currentPage - 1) * itemsPerPage + 1} to{' '}
            {Math.min(currentPage * itemsPerPage, totalItems)} of {totalItems} transactions
          </div>

          {/* Page navigation */}
          <div className="flex items-center gap-2">
            <button
              onClick={() => handlePageChange(1)}
              disabled={currentPage === 1}
              className="px-3 py-1.5 text-sm border border-cap-grey-400 rounded bg-cap-white text-cap-text hover:bg-cap-grey-100 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
              title="First page"
            >
              ««
            </button>
            <button
              onClick={() => handlePageChange(currentPage - 1)}
              disabled={currentPage === 1}
              className="px-3 py-1.5 text-sm border border-cap-grey-400 rounded bg-cap-white text-cap-text hover:bg-cap-grey-100 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
              title="Previous page"
            >
              ‹
            </button>

            {/* Page numbers */}
            <div className="flex gap-1">
              {Array.from({ length: Math.min(5, totalPages) }, (_, i) => {
                let pageNum;
                if (totalPages <= 5) {
                  pageNum = i + 1;
                } else if (currentPage <= 3) {
                  pageNum = i + 1;
                } else if (currentPage >= totalPages - 2) {
                  pageNum = totalPages - 4 + i;
                } else {
                  pageNum = currentPage - 2 + i;
                }

                return (
                  <button
                    key={pageNum}
                    onClick={() => handlePageChange(pageNum)}
                    className={`px-3 py-1.5 text-sm border rounded transition-colors ${
                      currentPage === pageNum
                        ? 'bg-cap-deep-blue text-white border-cap-deep-blue font-semibold'
                        : 'bg-cap-white text-cap-text border-cap-grey-400 hover:bg-cap-grey-100'
                    }`}
                  >
                    {pageNum}
                  </button>
                );
              })}
            </div>

            <button
              onClick={() => handlePageChange(currentPage + 1)}
              disabled={currentPage === totalPages}
              className="px-3 py-1.5 text-sm border border-cap-grey-400 rounded bg-cap-white text-cap-text hover:bg-cap-grey-100 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
              title="Next page"
            >
              ›
            </button>
            <button
              onClick={() => handlePageChange(totalPages)}
              disabled={currentPage === totalPages}
              className="px-3 py-1.5 text-sm border border-cap-grey-400 rounded bg-cap-white text-cap-text hover:bg-cap-grey-100 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
              title="Last page"
            >
              »»
            </button>
          </div>
        </div>
      )}
    </div>
  );
}
