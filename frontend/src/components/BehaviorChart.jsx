import { useEffect, useState } from 'react'

import {
    BarChart,
    Bar,
    XAxis,
    YAxis,
    CartesianGrid,
    Tooltip,
    ResponsiveContainer
} from 'recharts'

import './PnlChart.css'

function BehaviorChart() {

    const [data, setData] = useState([])


    useEffect(() => {
        async function loadBehavior() {
            try {
                const accountsResponse = await fetch(
                    'http://localhost:8080/api/broker-accounts',
                    { credentials: 'include' }
                );

                if (!accountsResponse.ok) {
                    throw new Error(`Failed to load accounts: HTTP ${accountsResponse.status}`);
                }

                const accounts = await accountsResponse.json();

                if (!Array.isArray(accounts) || accounts.length === 0) {
                    setData([]);
                    return;
                }

                const accountId = accounts[0].id;

                const response = await fetch(
                    `http://localhost:8080/api/analytics/behavior/${accountId}`,
                    { credentials: 'include' }
                );

                if (!response.ok) {
                    throw new Error(`Failed to load behavior: HTTP ${response.status}`);
                }

                const result = await response.json();

                setData([
                    { behavior: 'Early Exits', count: Number(result.earlyExits ?? 0) },
                    { behavior: 'Late Entries', count: Number(result.lateEntries ?? 0) },
                    { behavior: 'Stop Loss Hits', count: Number(result.stopLossHits ?? 0) },
                    { behavior: 'Target Hits', count: Number(result.targetHits ?? 0) }
                ]);
            } catch (error) {
                console.error('Error loading behavior chart:', error);
            }
        }

        loadBehavior();
    }, []);


    if (data === null) {
        return <p>Loading behavior chart...</p>
    }

    return (

        <div className="pnl-chart-card">
            <div className="pnl-chart-heading">
                <h3>Trading Behavior Breakdown</h3>
                <p>Patterns observed across trades with saved decisions</p>
            </div>

            <ResponsiveContainer width="100%" height={300}>
                <BarChart data={data}>
                    <CartesianGrid strokeDasharray="3 3" vertical={false} />
                    <XAxis dataKey="behavior" />
                    <YAxis allowDecimals={false} />
                    <Tooltip />
                    <Bar
                        dataKey="count"
                        fill="#2563eb"
                        radius={[5, 5, 0, 0]}
                        maxBarSize={70}
                    />
                </BarChart>
            </ResponsiveContainer>

        </div>
    )

}

export default BehaviorChart