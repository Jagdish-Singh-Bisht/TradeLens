import './PnlChart.css'

import {useEffect, useState} from 'react'

import {
    LineChart,
    Line,
    XAxis,
    YAxis,
    CartesianGrid,
    Tooltip,
    ResponsiveContainer,
    ReferenceLine
} from 'recharts'

function PnlChart({brokerAccountId}) {

    const [data, setData] = useState([])


    useEffect(() => {
        if (!brokerAccountId) {
            setData([]);
            return;
        }

        async function loadPnlTrend() {
            try {
                const response = await fetch(
                    `http://localhost:8080/api/broker-accounts/${brokerAccountId}/analytics/pnl-trend`,
                    { credentials: 'include' }
                );

                if (!response.ok) {
                    throw new Error(
                        `Failed to fetch P&L data: HTTP ${response.status}`
                    );
                }

                const result = await response.json();

                if (!Array.isArray(result)) {
                    throw new Error('Expected P&L trend data to be a list.');
                }

                setData(result);

            } catch (error) {
                console.error('Error loading P&L trend:', error);
                setData([]);
            }
        }

        loadPnlTrend();
    }, [brokerAccountId]);


    return (

        <div className="pnl-chart-card">
            <div className="pnl-chart-heading">
                <h3>P&L Performance</h3>
                <p>Cumulative profit and loss across completed trades</p>
            </div>

            <ResponsiveContainer width="100%" height={350}>

                <LineChart data={data}>

                    <CartesianGrid strokeDasharray="3 3" />

                    <ReferenceLine
                        y={0}
                        stroke="#6b7280"
                        strokeDasharray="4 4"
                    />

                    <XAxis
                        dataKey = "tradeNumber"
                        label={{
                            value: 'Trade',
                            position: 'insideBottom',
                            offset: -5
                        }}
                    />

                    <YAxis />

                    <Tooltip

                        formatter={(value) => [
                            `Rs.${Number(value).toLocaleString('en-IN', {
                                maximumFractionDigits: 2
                            })}`,
                            'Cumulative P&L'
                        ]}

                        labelFormatter={(label) => `Trade ${label}`}
                    />

                    <Line
                        type="monotone"
                        dataKey="cumulativeProfitLoss"
                        stroke="#2563eb"
                        strokeWidth={2.5}
                        dot={{ r: 4}}
                        activeDot={{r : 7}}
                    />

                </LineChart>

            </ResponsiveContainer>

        </div>
    )

}

export default PnlChart