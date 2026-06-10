interface StatCardProps {
  title: string;
  value: string | number;
  subtitle?: string;
  icon?: React.ReactNode;
  trend?: 'up' | 'down' | 'neutral';
}

export default function StatCard({ title, value, subtitle, icon, trend }: StatCardProps) {
  const trendColors = {
    up: 'text-cap-red',        // Capitec uses red for success/positive
    down: 'text-cap-blue-800',
    neutral: 'text-cap-text-muted',
  };

  return (
    <div className="card hover:shadow-card-hover animate-fade-up">
      <div className="flex items-center">
        <div className="flex-shrink-0">
          {icon && <div className="text-cap-blue">{icon}</div>}
        </div>
        <div className="ml-5 w-0 flex-1">
          <dl>
            <dt className="text-sm font-medium text-cap-text-muted truncate">{title}</dt>
            <dd>
              <div className="text-3xl font-bold text-cap-deep-blue mt-2">{value}</div>
              {subtitle && (
                <div className={`text-sm mt-1 font-medium ${trend ? trendColors[trend] : 'text-cap-text-muted'}`}>
                  {subtitle}
                </div>
              )}
            </dd>
          </dl>
        </div>
      </div>
    </div>
  );
}
