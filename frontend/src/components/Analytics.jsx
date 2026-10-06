import './Analytics.css'
import SummaryCard from './SummaryCard'
import { useEffect, useState } from 'react'

function Analytics() {

    const [analytics, setAnalytics] = useState(null)

    useEffect(() => {
        fetch('http://localhost:8080/api/broker-accounts/1/analytics',
            {
                credentials: 'include'
            })
            .then(response => response.json())
            .then(data => setAnalytics(data))
            .catch(error => console.error('Error loading analytics: ', error))

    }, [])

    if(!analytics) {

        return (
            <main>
                <h2>Analytics</h2>
                <p>Loading analytics...</p>
            </main>
        )
    }

    return (

        <main>
            <h2>Trading Analytics</h2>

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

