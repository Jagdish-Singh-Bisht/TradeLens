
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

function Behavior() {
    const [behavior, setBehavior] = useState(null)
    const [error, setError] = useState('')


    useEffect(() => {
        async function loadBehavior() {
            try {
                setError('');

                // Get the logged-in user's broker accounts
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
                    setError(
                        'No broker account found. Create an account to view trading behavior.'
                    );
                    return;
                }

                // Use the actual account ID
                const brokerAccountId = accounts[0].id;

                const response = await fetch(
                    `http://localhost:8080/api/analytics/behavior/${brokerAccountId}`,
                    { credentials: 'include' }
                );

                if (!response.ok) {
                    throw new Error(
                        `Failed to load behavior analytics: HTTP ${response.status}`
                    );
                }

                const data = await response.json();
                setBehavior(data);

            } catch (error) {
                console.error('Error loading behavior:', error);
                setError(error.message);
            }
        }

        loadBehavior();

    }, []);


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
