import './Analytics.css'
import SummaryCard from './SummaryCard'
import { useEffect, useState } from 'react'

function Analytics() {

    const [analytics, setAnalytics] = useState(null)


    useEffect(() => {
        async function loadAnalytics() {
            try {
                const accountsResponse = await fetch(
                    'http://localhost:8080/api/broker-accounts',
                    { credentials: 'include' }
                );

                if (!accountsResponse.ok) {
                    throw new Error(
                        `Failed to load accounts: HTTP ${accountsResponse.status}`
                    );
                }

                const accounts = await accountsResponse.json();

                if (!Array.isArray(accounts) || accounts.length === 0) {
                    setAnalytics(null);
                    return;
                }

                const accountId = accounts[0].id;

                const response = await fetch(
                    `http://localhost:8080/api/broker-accounts/${accountId}/analytics`,
                    { credentials: 'include' }
                );

                if (!response.ok) {
                    throw new Error(
                        `Failed to load analytics: HTTP ${response.status}`
                    );
                }

                const result = await response.json();
                setAnalytics(result);
            } catch (error) {
                console.error('Error loading analytics:', error);
            }
        }

        loadAnalytics();
    }, []);



    if (!analytics) {
        return (
            <main>
                <h2>Analytics</h2>
                <p>Loading analytics...</p>
            </main>
        );
    }

    if (analytics.message) {
        return (
            <main className="analytics">
                <h2>Trading Analytics</h2>
                <p>
                    {analytics.message === 'NO_BROKER_ACCOUNT'
                        ? 'No broker account found. Analytics will be available after an account is created.'
                        : analytics.message}
                </p>
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

