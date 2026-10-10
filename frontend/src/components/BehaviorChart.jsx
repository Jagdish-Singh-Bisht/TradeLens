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

    const [data, setData] = useState([null])

    useEffect(() => {
        fetch('http://localhost:8080/api/analytics/behavior/1',
            {
                credentials: 'include'
            })
            .then(response => {
                if(!response.ok) {
                    throw new Error('Failed to fetch trading behavior')
                }
                return response.json()
            })
            .then(result => {
                setData([
                    { behavior: 'Early Exits', count: result.earlyExits },
                    { behavior: 'Late Entries', count: result.lateEntries },
                    { behavior: 'Stop Loss Hits', count: result.stopLossHits },
                    { behavior: 'Target Hits', count: result.targetHits }
                ])
            })
            .catch(error => console.error(error))

    }, [])

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