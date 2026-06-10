'use client';

import ProtectedRoute from '@/components/ProtectedRoute';
import Link from 'next/link';
import { usePathname } from 'next/navigation';
import { TokenManager } from '@/lib/auth/token-manager';

export default function DashboardLayout({ children }: { children: React.ReactNode }) {
  const pathname = usePathname();

  const navigation = [
    { name: 'Dashboard', href: '/dashboard', current: pathname === '/dashboard' },
    { name: 'Rules', href: '/dashboard/rules', current: pathname?.startsWith('/dashboard/rules') },
    { name: 'Transactions', href: '/dashboard/transactions', current: pathname?.startsWith('/dashboard/transactions') },
  ];

  return (
    <ProtectedRoute>
      <div className="min-h-screen bg-cap-background">
        {/* Navigation Bar - Capitec Style */}
        <nav className="bg-cap-white shadow-sm">
          <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
            <div className="flex justify-between h-16">
              <div className="flex">
                <div className="flex-shrink-0 flex items-center">
                  <h1 className="text-xl font-bold text-cap-deep-blue">Fraud Rule Engine</h1>
                </div>
                <div className="hidden sm:ml-8 sm:flex sm:space-x-8">
                  {navigation.map((item) => (
                    <Link
                      key={item.name}
                      href={item.href}
                      className={`${
                        item.current
                          ? 'border-cap-blue text-cap-deep-blue'
                          : 'border-transparent text-cap-text-muted hover:border-cap-grey-400 hover:text-cap-text'
                      } inline-flex items-center px-1 pt-1 border-b-2 text-sm font-medium transition-colors duration-200`}
                    >
                      {item.name}
                    </Link>
                  ))}
                </div>
              </div>
              <div className="flex items-center">
                <button
                  onClick={() => TokenManager.logout()}
                  className="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md text-white bg-red-600 hover:bg-red-700 transition-colors duration-200"
                >
                  Logout
                </button>
              </div>
            </div>
          </div>
        </nav>

        {/* Main Content */}
        <main className="max-w-7xl mx-auto py-6 sm:px-6 lg:px-8">
          {children}
        </main>
      </div>
    </ProtectedRoute>
  );
}
