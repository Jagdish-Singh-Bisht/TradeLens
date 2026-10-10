import BehaviorChart from './BehaviorChart'
import StrategyChart from './StrategyChart'
import TradePnlChart from './TradePnlChart'
import PnlChart from './PnlChart'
import { useEffect, useState } from 'react'
import SummaryCard from './SummaryCard'
import './Dashboard.css'



function Dashboard() {

    const [analytics, setAnalytics] = useState(null)
    const [strategies, setStrategies] = useState(null)
    const[behavior, setBehavior] = useState(null)

    useEffect(() => {

        Promise.all([

            fetch('http://localhost:8080/api/broker-accounts/1/analytics',
                {
                    credentials: 'include'
                })
                .then(response => response.json()),

            fetch('http://localhost:8080/api/analytics/strategies/1',
                {
                    credentials: 'include'
                })
                .then(response => response.json()),

            fetch('http://localhost:8080/api/analytics/behavior/1',
                {
                    credentials: 'include'
                })
                .then(response => response.json())

        ])
            .then(([analyticsData, strategiesData, behaviorData]) => {
                setAnalytics(analyticsData)
                setStrategies(strategiesData)
                setBehavior(behaviorData)
            })
            .catch(error => console.error('Error loading dashboard: ', error))

    }, [])

    if(!analytics || !strategies || !behavior) {

        return (
            <main className="dashboard">
                <div className="dashboard-heading">
                    <h2>Trading Overview</h2>
                    <p>Review your overall trading performance.</p>
                </div>

                <p>Loading dashboard...</p>
            </main>
        )

    }

    const cards = [
        {
            title: 'Total P&L',
            value: `Rs.${analytics.totalProfitLoss}`,
            type: analytics.totalProfitLoss >= 0 ? 'profit' : 'loss'
        },
        { title: 'Win Rate', value: `${analytics.winRate}%` },
        { title: 'Total Trades', value: analytics.totalTrades },
        { title: 'Profit Factor', value: analytics.profitFactor || '-' }
    ]

    return (
        <main className="dashboard">

            <div className="dashboard-heading">
                <h2>Trading Overview</h2>
                <p>Review your overall trading performance.</p>
            </div>

            <section className="dashboard-section">

                <div className="dashboard-section-header">
                    <h3>Performance</h3>
                </div>

                <div className="dashboard-cards">

                    {cards.map((card) => (
                        <SummaryCard
                            key={card.title}
                            title={card.title}
                            value={card.value}
                        />
                    ))}

                </div>

            </section>

            <section className="dashboard-section">
                <PnlChart />
            </section>

            <section className="dashboard-section">
                <TradePnlChart />
            </section>

            <section className="dashboard-section">

                <div className="dashboard-section-header">
                    <h3>Strategy Performance</h3>
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

                                {strategies.map((strategy) => (
                                    <tr key={strategy.strategy}>
                                        <td>{strategy.strategy}</td>
                                        <td>{strategy.analytics.totalTrades}</td>
                                        <td>{strategy.analytics.winRate}%</td>
                                        <td>Rs.{strategy.analytics.totalProfitLoss}</td>
                                        <td>{strategy.analytics.profitFactor}</td>
                                    </tr>
                                ))}

                            </tbody>

                        </table>

                    </div>

                )}

                <StrategyChart />

            </section>

            <section className="dashboard-section">

                <div className="dashboard-section-header">
                    <h3>Trading Behavior</h3>
                </div>

                <div className="dashboard-cards">

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

                <BehaviorChart />

            </section>

        </main>
    )
}

export default Dashboard