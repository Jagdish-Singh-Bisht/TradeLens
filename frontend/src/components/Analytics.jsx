import { useBrokerAccount } from '../context/BrokerAccountContext';
import './Analytics.css'
import SummaryCard from './SummaryCard'
import { useEffect, useState } from 'react'

import { useDateRange } from '../context/DateRangeContext'

function Analytics() {

    const {
        selectedBrokerAccountId,
        loading: accountsLoading,
        error: accountsError
    } = useBrokerAccount();

    const {
        startDate,
        endDate,
        dateFilterError
    } = useDateRange()

    const [analytics, setAnalytics] = useState(null)
    const [analyticsError, setAnalyticsError] = useState(null)

    const [riskAnalytics, setRiskAnalytics] = useState(null)
    const [riskAnalyticsError, setRiskAnalyticsError] = useState(null)

    useEffect(() => {

        if(accountsLoading) return;

        if (dateFilterError) {
            setAnalytics(null)
            setAnalyticsError(null)
            return
        }

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

                const dateQuery =
                    startDate && endDate
                        ? `?startDate=${startDate}&endDate=${endDate}`
                        : ''

                const analyticsUrl =
                    selectedBrokerAccountId === 'ALL'
                        ? `http://localhost:8080/api/analytics/consolidated${dateQuery}`
                        : `http://localhost:8080/api/broker-accounts/${selectedBrokerAccountId}/analytics${dateQuery}`

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

    }, [selectedBrokerAccountId, accountsLoading, accountsError, startDate, endDate, dateFilterError]);

    useEffect(() => {

        if(accountsLoading) return;

        if(dateFilterError || accountsError || !selectedBrokerAccountId) {
            setRiskAnalytics(null);
            setRiskAnalyticsError(null);
            return;
        }

        let cancelled = false;

        async function loadRiskAnalytics() {

            try {
                setRiskAnalytics(null);
                setRiskAnalyticsError(null);

                const params = new URLSearchParams();

                if(selectedBrokerAccountId !== 'ALL') {
                    params.append('brokerAccountId', selectedBrokerAccountId);
                }

                if(startDate && endDate) {
                    params.append('startDate', startDate);
                    params.append('endDate', endDate);
                }

                const queryString = params.toString();

                const url = `http://localhost:8080/api/risk-analytics${queryString ? `?${queryString}` : '' }`;

                const response = await fetch(url, {
                    credentials: 'include'
                })

                if(!response.ok) {
                    throw new Error(
                        `Failed to load risk analytics: HTTP ${response.status}`
                    );
                }

                const result = await response.json();

                if(!cancelled) {
                    setRiskAnalytics(result);
                }

            } catch (error) {

                if(!cancelled) {
                    console.error('Error loading risk analytics:', error);
                    setRiskAnalyticsError(error.message || 'Unable to load risk analytics.');
                }
            }
        }

        loadRiskAnalytics();

        return () => {
            cancelled = true;
        }

    }, [selectedBrokerAccountId, accountsLoading, accountsError, startDate, endDate, dateFilterError]);


    if (dateFilterError) {
        return (
            <main className="analytics">
                <h2>Trading Analytics</h2>
                <p>{dateFilterError}</p>
                <p>Change the date range from the Dashboard.</p>
            </main>
        )
    }

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

            <section className="risk-analytics-section">

                <div className="risk-analytics-heading">
                    <h3>Risk Analytics</h3>
                    <p>
                        Understand trading consistency, downside risk, and recovery performance.
                    </p>
                </div>

                {riskAnalyticsError ? (
                    <p className="risk-analytics-error">{riskAnalyticsError}</p>
                ) : !riskAnalytics ? (
                    <p>Loading risk analytics...</p>
                ) : (
                    <div className="analytics-grid">

                        <SummaryCard
                            title="Trading Expectancy"
                            value={`Rs.${riskAnalytics.expectancy}`}
                        />

                        <SummaryCard
                            title="Maximum Drawdown"
                            value={`Rs.${riskAnalytics.maximumDrawdown}`}
                            type="loss"
                        />

                        <SummaryCard
                          title="Maximum Consecutive Wins"
                          value={riskAnalytics.maximumConsecutiveWins}
                        />

                        <SummaryCard
                            title="Maximum Consecutive Losses"
                            value={riskAnalytics.maximumConsecutiveLosses}
                        />

                        <SummaryCard
                            title="Recovery Factor"
                            value={riskAnalytics.recoveryFactor ?? 'N/A'}
                        />

                    </div>

                )}

            </section>

        </main>

    )

}

export default Analytics

