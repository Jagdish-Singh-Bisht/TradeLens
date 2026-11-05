
    import './Behavior.css'
    import { useEffect, useState } from 'react'
    import SummaryCard from './SummaryCard'
    import {
        ResponsiveContainer,
        BarChart,
        Bar,
        XAxis,
        YAxis,
        CartesianGrid,
        Tooltip
    } from 'recharts'

    import { useBrokerAccount } from '../context/BrokerAccountContext'


    function Behavior() {

        const {
            selectedBrokerAccountId,
            loading: accountsLoading,
            error: accountsError
        } = useBrokerAccount();

        const [behavior, setBehavior] = useState(null)
        const [error, setError] = useState('')

        useEffect(() => {

            if(accountsLoading) return;

            if(accountsError) {
                setBehavior(null);
                setError(accountsError);
                return;
            }

            if(!selectedBrokerAccountId) {
                setBehavior(null);
                setError('No broker account selected.');
                return;
            }

            let cancelled = false;

            async function loadBehavior() {

                try {
                    setBehavior(null);
                    setError('');

                    const behaviorUrl =
                        selectedBrokerAccountId === 'ALL'
                            ? 'http://localhost:8080/api/analytics/behavior/consolidated'
                            : `http://localhost:8080/api/analytics/behavior/${selectedBrokerAccountId}`;

                    const response = await fetch(
                        behaviorUrl,
                        { credentials: 'include' }
                    );

                    if (!response.ok) {
                        throw new Error(
                            `Failed to load behavior analytics: HTTP ${response.status}`
                        );
                    }

                    const data = await response.json();

                    if(!cancelled) {
                        setBehavior(data);
                    }

                } catch (error) {
                    if(!cancelled) {
                        console.error('Error loading behavior:', error);
                        setError(error.message || 'Unable to load behavior analytics.');
                    }
                }
            }

            loadBehavior();

            return () => {
                cancelled = true;
            }

        }, [selectedBrokerAccountId, accountsLoading, accountsError]);

        if (accountsLoading) {
            return (
                <main className="behavior">
                    <h2>Trading Behavior</h2>
                    <p>Loading broker accounts...</p>
                </main>
            );
        }

        if (accountsError) {
            return (
                <main className="behavior">
                    <h2>Trading Behavior</h2>
                    <p>{accountsError}</p>
                </main>
            );
        }

        if (!selectedBrokerAccountId) {
            return (
                <main className="behavior">
                    <h2>Trading Behavior</h2>
                    <p>No broker account selected.</p>
                </main>
            );
        }

        if (error) {
            return (
                <main className="behavior">
                    <h2>Trading Behavior</h2>
                    <p>{error}</p>
                </main>
            )
        }

        if (!behavior) {
            return (
                <main className="behavior">
                    <h2>Trading Behavior</h2>
                    <p>Loading behavior...</p>
                </main>
            )
        }

        const chartData = [
            { category: 'Early Exits', count: Number(behavior.earlyExits ?? 0) },
            { category: 'Late Entries', count: Number(behavior.lateEntries ?? 0) },
            { category: 'Stop Loss Hits', count: Number(behavior.stopLossHits ?? 0) },
            { category: 'Target Hits', count: Number(behavior.targetHits ?? 0) }
        ]

        return (
            <main className="behavior">
                <div className="behavior-heading">
                    <h2>Trading Behavior</h2>
                    <p>Understand recurring patterns in your trading decisions.</p>
                </div>

                <div className="behavior-grid">
                    <SummaryCard
                        title="Total Trades"
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

                <section className="behavior-chart-container">
                    <h3>Behavior Breakdown</h3>
                    <p>Number of trades recorded in each behavior category.</p>

                    <ResponsiveContainer width="100%" height={320}>
                        <BarChart
                            data={chartData}
                            margin={{
                                top: 16,
                                right: 24,
                                left: 8,
                                bottom: 8
                            }}
                        >
                            <CartesianGrid strokeDasharray="3 3" />
                            <XAxis dataKey="category" />
                            <YAxis allowDecimals={false} />
                            <Tooltip />
                            <Bar
                                dataKey="count"
                                name="Trade Count"
                                fill="#2563eb"
                                barSize={48}
                                radius={[5, 5, 0, 0]}
                            />
                        </BarChart>
                    </ResponsiveContainer>
                </section>
            </main>
        )
    }

    export default Behavior
