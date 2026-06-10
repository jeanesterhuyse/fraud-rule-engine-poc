'use client';

import { useEffect, useState } from 'react';
import Link from 'next/link';
import { dashboardService } from '@/lib/api/dashboard';
import { DashboardSummary } from '@/types/api';
import StatCard from '@/components/StatCard';

export default function DashboardPage() {
  const [summary, setSummary] = useState<DashboardSummary | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    loadDashboardData();
  }, []);

  const loadDashboardData = async () => {
    try {
      setLoading(true);
      const data = await dashboardService.getSummary();
      setSummary(data);
      setError(null);
    } catch (err: any) {
      setError(err.message || 'Failed to load dashboard data');
      console.error('Dashboard error:', err);
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="text-center">
          <div className="spinner-lg"></div>
          <p className="mt-4 text-cap-text-muted">Loading dashboard...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="card border-l-4 border-cap-red">
        <p className="text-cap-red font-medium">Error: {error}</p>
        <button
          onClick={loadDashboardData}
          className="mt-2 text-sm text-cap-red hover:text-cap-red-600 underline"
        >
          Try again
        </button>
      </div>
    );
  }

  if (!summary) {
    return null;
  }

  return (
    <div>
      <div className="px-4 py-6 sm:px-0">
        <div className="flex items-center justify-between mb-6">
          <h1 className="cap-page-title mb-0">
            Fraud Detection Dashboard
          </h1>
          <button
            onClick={loadDashboardData}
            className="btn-outline"
            disabled={loading}
          >
            {loading ? (
              <span className="flex items-center gap-2">
                <div className="spinner"></div>
                Refreshing...
              </span>
            ) : (
              <span className="flex items-center gap-2">
                <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
                </svg>
                Refresh
              </span>
            )}
          </button>
        </div>

        {/* Summary Cards - Capitec Style */}
        <div className="grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-4 mb-8">
          <StatCard
            title="Total Triggered Transactions"
            value={summary.totalTriggeredTransactions.toLocaleString()}
            subtitle="All time"
          />
          <StatCard
            title="Active Rules"
            value={summary.totalActiveRules}
            subtitle="Currently enabled"
            trend="neutral"
          />
          <StatCard
            title="Last 24 Hours"
            value={summary.totalTriggersLast24Hours}
            subtitle="Recent triggers"
            trend={summary.totalTriggersLast24Hours > 0 ? 'up' : 'neutral'}
          />
          <StatCard
            title="Average Risk Score"
            value={summary.averageRiskScore.toFixed(1)}
            subtitle={`Highest: ${summary.highestRiskScore}`}
          />
        </div>

        {/* Additional Stats - Capitec Card Style */}
        <div className="grid grid-cols-1 gap-6 sm:grid-cols-3 mb-8">
          <div className="card cap-card-info">
            <h3 className="cap-section-title">Last 7 Days</h3>
            <p className="text-4xl font-bold text-cap-deep-blue">
              {summary.totalTriggersLast7Days.toLocaleString()}
            </p>
            <p className="text-sm text-cap-text-muted mt-2">Triggered transactions</p>
          </div>
          <div className="card cap-card-success">
            <h3 className="cap-section-title">Total Flagged Amount</h3>
            <p className="text-4xl font-bold text-cap-deep-blue">
              {summary.currency} {summary.totalFlaggedAmount.toLocaleString()}
            </p>
            <p className="text-sm text-cap-text-muted mt-2">Across all transactions</p>
          </div>
          <div className="card hover:shadow-card-hover transition-all duration-200">
            <h3 className="cap-section-title">Avg Triggers per Rule</h3>
            <p className="text-4xl font-bold text-cap-deep-blue">
              {summary.totalActiveRules > 0
                ? (summary.totalTriggersLast24Hours / summary.totalActiveRules).toFixed(1)
                : '0.0'}
            </p>
            <p className="text-sm text-cap-text-muted mt-2">
              Per rule in last 24 hours
            </p>
          </div>
        </div>

        {/* Quick Links */}
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 mb-8">
          <Link
            href="/dashboard/rules"
            className="card hover:shadow-card-hover transition-all duration-200 group"
          >
            <div className="flex items-center justify-between">
              <div>
                <h3 className="text-lg font-bold text-cap-deep-blue group-hover:text-cap-blue transition-colors">
                  Manage Rules
                </h3>
                <p className="text-sm text-cap-text-muted mt-1">
                  Create, edit, and configure detection rules
                </p>
              </div>
              <svg className="w-6 h-6 text-cap-blue" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
              </svg>
            </div>
          </Link>
          <Link
            href="/dashboard/transactions"
            className="card hover:shadow-card-hover transition-all duration-200 group"
          >
            <div className="flex items-center justify-between">
              <div>
                <h3 className="text-lg font-bold text-cap-deep-blue group-hover:text-cap-blue transition-colors">
                  View Transactions
                </h3>
                <p className="text-sm text-cap-text-muted mt-1">
                  Review triggered transactions and alerts
                </p>
              </div>
              <svg className="w-6 h-6 text-cap-blue" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
              </svg>
            </div>
          </Link>
        </div>

        {/* Info Box - Capitec Info Style */}
        <div className="mt-6 card cap-card-info">
          <p className="text-sm text-cap-text">
            <strong className="text-cap-deep-blue">Live System:</strong> The fraud detection engine is actively processing transactions.
            Data is updated in real-time as transactions trigger rules.
          </p>
        </div>
      </div>
    </div>
  );
}
