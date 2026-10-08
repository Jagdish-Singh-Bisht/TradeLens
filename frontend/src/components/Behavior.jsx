import './Behavior.css'
import { useEffect, useState } from 'react'
import SummaryCard from './SummaryCard'

function Behavior() {
    const [behavior, setBehavior] = useState(null)

    useEffect(() => {
        fetch('http://localhost:8080/api/analytics/behavior/1', {
            credentials: 'include'
        })
            .then(response => response.json())
            .then(data => setBehavior(data))
            .catch(error => console.error('Error loading behavior:', error))
    }, [])

    if (!behavior) {
        return (
            <main>
                <h2>Behavior</h2>
                <p>Loading behavior...</p>
            </main>
        )
    }

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
        </main>
    )
}

export default Behavior