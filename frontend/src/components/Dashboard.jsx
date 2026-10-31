import BrokerAccountForm from './BrokerAccountForm';
import BehaviorChart from './BehaviorChart';
import StrategyChart from './StrategyChart';
import TradePnlChart from './TradePnlChart';
import PnlChart from './PnlChart';
import { useEffect, useState } from 'react';
import SummaryCard from './SummaryCard';
import './Dashboard.css';

function Dashboard() {
    const [analytics, setAnalytics] = useState(null);
    const [strategies, setStrategies] = useState(null);
    const [behavior, setBehavior] = useState(null);
    const [error, setError] = useState(null);
    const [loading, setLoading] = useState(true);
    const [refreshKey, setRefreshKey] = useState(0);

    useEffect(() => {
        async function loadDashboard() {
            try {
                setLoading(true);
                setError(null);

                // Step 1: Fetch this user's broker accounts
                const accountsResponse = await fetch(
                    'http://localhost:8080/api/broker-accounts',
                    { credentials: 'include' }
                );

                if (!accountsResponse.ok) {
                    throw new Error(
                        `Could not load broker accounts: HTTP ${accountsResponse.status}`
                    );
                }

                const accounts = await accountsResponse.json();

                if (!Array.isArray(accounts)) {
                    throw new Error('Broker accounts API did not return a list.');
                }

                if (accounts.length === 0) {
                    setAnalytics(null);
                    setStrategies(null);
                    setBehavior(null);
                    setError('NO_BROKER_ACCOUNT');
                    return;
                }

                const brokerAccountId = accounts[0].id;

                const urls = [
                    `http://localhost:8080/api/broker-accounts/${brokerAccountId}/analytics`,
                    `http://localhost:8080/api/analytics/strategies/${brokerAccountId}`,
                    `http://localhost:8080/api/analytics/behavior/${brokerAccountId}`
                ];

                const responses = await Promise.all(
                    urls.map(url =>
                        fetch(url, { credentials: 'include' })
                    )
                );

                const failedResponse = responses.find(
                    response => !response.ok
                );

                if (failedResponse) {
                    throw new Error(
                        `Dashboard API failed: HTTP ${failedResponse.status}`
                    );
                }

                const [analyticsData, strategiesData, behaviorData] = await Promise.all(
                    responses.map(response => response.json())
                );

                if (!Array.isArray(strategiesData)) {
                    throw new Error(
                        'Expected the strategies API to return a list.'
                    );
                }

                setAnalytics(analyticsData);
                setStrategies(strategiesData);
                setBehavior(behaviorData);

            } catch (err) {
                console.error('Error loading dashboard:', err);
                setError(err.message || 'Unable to load dashboard.');
            } finally {
                setLoading(false);
            }
        }

        loadDashboard();
    }, [refreshKey]);

    if (loading) {
        return (
            <main className="dashboard">
                <h2>Trading Overview</h2>
                <p>Loading dashboard...</p>
            </main>
        );
    }

    if (error) {
        return (
            <main className="dashboard">
                <div className="dashboard-heading">
                    <h2>Trading Overview</h2>
                    <p>Review your overall trading performance.</p>
                </div>

                {error === 'NO_BROKER_ACCOUNT' ? (
                    <BrokerAccountForm
                        onCreated={() => {
                            setError(null);
                            setRefreshKey(previous => previous + 1);
                        }}
                    />
                ) : (
                    <div className="dashboard-empty">
                        <p>{error}</p>
                        <button onClick={() => setRefreshKey(previous => previous + 1)}>
                            Retry
                        </button>
                    </div>
                )}
            </main>
        );
    }

    const cards = [
        {
            title: 'Total P&L',
            value: `Rs.${analytics.totalProfitLoss}`,
            type: analytics.totalProfitLoss >= 0 ? 'profit' : 'loss'
        },
        {
            title: 'Win Rate',
            value: `${analytics.winRate}%`
        },
        {
            title: 'Total Trades',
            value: analytics.totalTrades
        },
        {
            title: 'Profit Factor',
            value: analytics.profitFactor ?? '-'
        }
    ];

    return (
        <main className="dashboard">
            <div className="dashboard-heading">
                <h2>Trading Overview</h2>
                <p>Review your overall trading performance.</p>
            </div>

            <section className="dashboard-section">
                <div className="dashboard-section-header">
                    <h3>Performance Overview</h3>
                </div>

                <div className="dashboard-cards">
                    {cards.map(card => (
                        <SummaryCard
                            key={card.title}
                            title={card.title}
                            value={card.value}
                            type={card.type}
                        />
                    ))}
                </div>
            </section>

            <section className="dashboard-section">
                <PnlChart />
            </section>

            <section className="dashboard-section">
                <div className="dashboard-section-header">
                    <h3>Trade & Strategy Analysis</h3>
                </div>

                <div className="dashboard-analysis-grid">
                    <TradePnlChart />
                    <StrategyChart />
                </div>

                <div className="dashboard-subsection">
                    <div className="dashboard-section-header">
                        <h3>Strategy Performance Details</h3>
                    </div>

                    {strategies.length === 0 ? (
                        <div className="dashboard-empty">
                            <p>No strategy data available.</p>
                        </div>
                    ) : (
                        <div className="dashboard-table-card">
                            <table className="dashboard-table">
                                <thead>
                                <tr>
                                    <th>Strategy</th>
                                    <th>Total Trades</th>
                                    <th>Win Rate</th>
                                    <th>P&L</th>
                                    <th>Profit Factor</th>
                                </tr>
                                </thead>

                                <tbody>
                                    {strategies.map((strategy, index) => (
                                        <tr key={strategy.strategy ?? index}>
                                            <td>{strategy.strategy}</td>
                                            <td>{strategy.analytics.totalTrades}</td>
                                            <td>{strategy.analytics.winRate}%</td>
                                            <td>Rs.{strategy.analytics.totalProfitLoss}</td>
                                            <td>{strategy.analytics.profitFactor ?? '-'}</td>
                                        </tr>
                                    ))}
                                </tbody>
                            </table>
                        </div>
                    )}

                </div>
            </section>

            <section className="dashboard-section">
                <div className="dashboard-section-header">
                    <h3>Trading Behavior</h3>
                </div>

                <div className="dashboard-cards dashboard-behavior-cards">
                    <SummaryCard
                        title="Trades with Decisions"
                        value={behavior.totalTrades}
                    />
                    <SummaryCard
                        title="Early Exits"
                        value={behavior.earlyExits}
                    />
                    <SummaryCard
                        title="Late Entries"
                        value={behavior.lateEntries}
                    />
                    <SummaryCard
                        title="Stop Loss Hits"
                        value={behavior.stopLossHits}
                    />
                    <SummaryCard
                        title="Target Hits"
                        value={behavior.targetHits}
                    />
                </div>

                <BehaviorChart />
            </section>
        </main>
    );
}

export default Dashboard;
