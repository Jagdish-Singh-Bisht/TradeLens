import { useEffect, useState } from 'react'
import SummaryCard from './SummaryCard'
import './Dashboard.css'



function Dashboard() {

    const [analytics, setAnalytics] = useState(null)

    const [strategies, setStrategies] = useState([])

    const[behavior, setBehavior] = useState(null)

    useEffect(() => {

        // Overall trading analytics
        fetch('http://localhost:8080/api/broker-accounts/1/analytics',
            {
                credentials: 'include'
            })
            .then(response => response.json())
            .then(data => {
                console.log('Analytics data:', data)
                setAnalytics(data)
            })

        //Strategy analytics
        fetch('http://localhost:8080/api/analytics/strategies/1',
            {
                credentials: 'include'
            })
            .then(response => response.json())
            .then(data => {
                console.log('Strategy data:', data)
                setStrategies(data)
            })

        // Behavior
        fetch('http://localhost:8080/api/analytics/behavior/1',
            {
                credentials: 'include'
            })
            .then(response => response.json())
            .then(data => {
                console.log('Behavior data:', data)
                setBehavior(data)
            })

    }, [])

    if(!analytics) {
        return <main>Loading...</main>
    }

    const cards = [
        { title: 'Total P&L', value: analytics.totalProfitLoss },
        { title: 'Win Rate', value: `${analytics.winRate}%` },
        { title: 'Total Trades', value: analytics.totalTrades },
        { title: 'Profit Factor', value: analytics.profitFactor }
    ]

    return (
        <main>
            <h2>Trading Overview</h2>

            <div className="summary-cards">

                {cards.map((card) => (
                    <SummaryCard
                        key={card.title}
                        title={card.title}
                        value={card.value}
                    />
                ))}

            </div>

            {/* Strategy Performance*/}
            <section>
                <h2>Strategy Performance</h2>

                {strategies.length === 0 ? (
                    <p>No strategy data available.</p>
                ) : (
                    <table>
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
                        {strategies.map((strategy) => (
                            <tr key={strategy.strategy}>
                                <td>{strategy.strategy}</td>
                                <td>{strategy.analytics.totalTrades}</td>
                                <td>{strategy.analytics.winRate}%</td>
                                <td>{strategy.analytics.totalProfitLoss}</td>
                                <td>{strategy.analytics.profitFactor}</td>
                            </tr>
                        ))}
                        </tbody>
                    </table>
                )}

            </section>

            <section className="behavior-section">
                <h2>Trading Behavior</h2>

                {!behavior ? (
                    <p>Loading behavior data...</p>
                ) : (

                    <div className="behavior-cards">
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
                )}

            </section>

        </main>
    )
}

export default Dashboard