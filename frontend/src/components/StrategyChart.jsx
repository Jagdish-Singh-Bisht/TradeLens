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

function StrategyChart({brokerAccountId}) {

    const [data, setData] = useState([])


    useEffect(() => {
        if (!brokerAccountId) {
            setData([]);
            return;
        }

        async function loadStrategies() {
            try {
                const response = await fetch(
                    `http://localhost:8080/api/analytics/strategies/${brokerAccountId}`,
                    { credentials: 'include' }
                );

                if (!response.ok) {
                    throw new Error(
                        `Failed to load strategies: HTTP ${response.status}`
                    );
                }

                const result = await response.json();

                if (!Array.isArray(result)) {
                    throw new Error('Expected strategy analytics to be a list.');
                }

                setData(result.map(item => ({
                    strategy: item.strategy,
                    totalProfitLoss: Number(item.analytics.totalProfitLoss ?? 0),
                    totalTrades: item.analytics.totalTrades,
                    winRate: Number(item.analytics.winRate ?? 0)
                })));

            } catch (error) {
                console.error('Error loading strategy chart:', error);
                setData([]);
            }
        }

        loadStrategies();
    }, [brokerAccountId]);


    return (

        <div className="pnl-chart-card">

            <div className="pnl-chart-heading">
                <h3>Strategy Performance</h3>
                <p>Compare profitability across trading strategies</p>
            </div>

            {data.length === 0 ? (
                <p>No strategy data available yet.</p>
            ) : (
                <ResponsiveContainer width="100%" height={300}>

                    <BarChart data={data}>

                        <CartesianGrid
                            strokeDasharray="3 3"
                            vertical={false}
                        />

                        <XAxis dataKey="strategy" />

                        <YAxis />


                        <Tooltip
                            content={({ active, payload }) => {
                                if (!active || !payload || !payload.length) {
                                    return null
                                }

                                const strategy = payload[0].payload

                                return (
                                    <div className="pnl-tooltip">
                                        <strong>{strategy.strategy}</strong>

                                        <p>
                                            Total P&L: ₹
                                            {strategy.totalProfitLoss.toLocaleString('en-IN')}
                                        </p>

                                        <p>
                                            Total Trades: {strategy.totalTrades}
                                        </p>

                                        <p>
                                            Win Rate: {strategy.winRate}%
                                        </p>
                                    </div>
                                )
                            }}
                        />


                        <Bar
                            dataKey="totalProfitLoss"
                            fill="#2563eb"
                            radius={[6, 6, 0, 0]}
                            maxBarSize={80}
                        />

                    </BarChart>
                </ResponsiveContainer>
            )}

        </div>
    )

}

export default StrategyChart