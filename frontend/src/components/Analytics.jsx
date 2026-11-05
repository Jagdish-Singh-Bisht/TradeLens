import { useBrokerAccount } from '../context/BrokerAccountContext';
import './Analytics.css'
import SummaryCard from './SummaryCard'
import { useEffect, useState } from 'react'

function Analytics() {

    const {
        selectedBrokerAccountId,
        loading: accountsLoading,
        error: accountsError
    } = useBrokerAccount();

    const [analytics, setAnalytics] = useState(null)
    const [analyticsError, setAnalyticsError] = useState(null)

    useEffect(() => {

        if(accountsLoading) return;

        if(accountsError) {
            setAnalytics(null);
            setAnalyticsError(accountsError);

            return;
        }

        if(!selectedBrokerAccountId) {
            setAnalytics(null);
            setAnalyticsError(null)

            return;
        }

        let cancelled = false;

        async function loadAnalytics() {

            try {
                setAnalytics(null)
                setAnalyticsError(null)

                const analyticsUrl =
                    selectedBrokerAccountId === 'ALL'
                        ? 'http://localhost:8080/api/analytics/consolidated'
                        : `http://localhost:8080/api/broker-accounts/${selectedBrokerAccountId}/analytics`;

                const response = await fetch(
                    analyticsUrl,
                    { credentials: 'include' }
                );

                if (!response.ok) {
                    throw new Error(
                        `Failed to load analytics: HTTP ${response.status}`
                    );
                }

                const result = await response.json();

                if(!cancelled) {
                    setAnalytics(result ?? {
                        totalTrades: 0,
                        winRate: 0,
                        totalProfitLoss: 0,
                        profitFactor: null,
                        averageProfit: 0,
                        averageLoss: 0
                    });
                }

            } catch (error) {
                if(!cancelled) {
                    console.error('Error loading analytics:', error);
                    setAnalyticsError(
                        error.message || 'Unable to load analytics.'
                    );
                }
            }
        }

        loadAnalytics();

        return () => {
            cancelled = true;
        };

    }, [selectedBrokerAccountId, accountsLoading, accountsError]);



    if (accountsLoading) {
        return (
            <main className="analytics">
                <h2>Trading Analytics</h2>
                <p>Loading broker accounts...</p>
            </main>
        );
    }

    if (accountsError) {
        return (
            <main className="analytics">
                <h2>Trading Analytics</h2>
                <p>{accountsError}</p>
            </main>
        );
    }

    if (!selectedBrokerAccountId) {
        return (
            <main className="analytics">
                <h2>Trading Analytics</h2>
                <p>No broker account selected. Create or select a broker account.</p>
            </main>
        );
    }

    if (analyticsError) {
        return (
            <main className="analytics">
                <h2>Trading Analytics</h2>
                <p>{analyticsError}</p>
            </main>
        );
    }

    if (!analytics) {
        return (
            <main className="analytics">
                <h2>Trading Analytics</h2>
                <p>Loading analytics...</p>
            </main>
        );
    }

    if (analytics.message) {
        return (
            <main className="analytics">
                <h2>Trading Analytics</h2>
                <p>{analytics.message}</p>
            </main>
        );
    }

    return (

        <main className="analytics">

            <div className="analytics-heading">
                <h2>Trading Analytics</h2>
                <p>Detailed view of your overall trading performance.</p>
            </div>

            <div className="analytics-grid">

                <SummaryCard
                    title="Total Trades"
                    value={analytics.totalTrades}
                />

                <SummaryCard
                    title="Win Rate"
                    value={`${analytics.winRate}%`}
                />

                <SummaryCard
                    title="Total P&L"
                    value={`Rs.${analytics.totalProfitLoss}`}
                />

                <SummaryCard
                    title="Profit Factor"
                    value={analytics.profitFactor}
                />

                <SummaryCard
                    title="Average Profit"
                    value={`Rs.${analytics.averageProfit}`}
                />

                <SummaryCard
                    title="Average Loss"
                    value={`Rs.${analytics.averageLoss}`}
                />

            </div>

        </main>

    )

}

export default Analytics

