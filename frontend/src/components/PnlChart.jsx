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

function PnlChart({ brokerAccountId, startDate, endDate }) {

    const [data, setData] = useState([])


    useEffect(() => {
        if (!brokerAccountId) {
            setData([]);
            return;
        }

        let cancelled = false;
        setData([]);

        async function loadPnlTrend() {
            try {

                const dateQuery =
                    startDate && endDate
                        ? `?startDate=${startDate}&endDate=${endDate}`
                        : '';

                const pnlTrendUrl =
                    brokerAccountId === 'ALL'
                        ? `http://localhost:8080/api/analytics/consolidated/pnl-trend${dateQuery}`
                        : `http://localhost:8080/api/broker-accounts/${brokerAccountId}/analytics/pnl-trend${dateQuery}`;

                const response = await fetch(
                    pnlTrendUrl,
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

                if (!cancelled) {
                    setData(result);
                }

            } catch (error) {
                if (!cancelled) {
                    console.error('Error loading P&L trend:', error);
                    setData([]);
                }
            }
        }

        loadPnlTrend();

        return () => {
            cancelled = true;
        };

    }, [brokerAccountId, startDate, endDate]);


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