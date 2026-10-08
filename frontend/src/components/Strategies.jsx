import './Strategies.css'
import { useEffect, useState } from 'react'

function Strategies() {
    const [strategies, setStrategies] = useState(null)

    useEffect(() => {
        fetch('http://localhost:8080/api/analytics/strategies/1', {
            credentials: 'include'
        })
            .then(response => response.json())
            .then(data => setStrategies(data))
            .catch(error => console.error('Error loading strategies:', error))
    }, [])

    if (!strategies) {
        return (
            <main>
                <h2>Strategies</h2>
                <p>Loading strategies...</p>
            </main>
        )
    }

    return (

        <main className="strategies">

            <div className="strategy-heading">
                <h2>Strategy Performance</h2>
                <p>Compare performance across your trading strategies.</p>
            </div>

            <div className="strategy-table-container">

                <table className="strategy-table">

                    <thead>
                        <tr>
                            <th>Strategy</th>
                            <th>Total Trades</th>
                            <th>Win Rate</th>
                            <th>Total P&L</th>
                            <th>Profit Factor</th>
                        </tr>
                    </thead>

                    <tbody>

                        {strategies.map((item) => (

                            <tr key={item.strategy}>
                                <td>{item.strategy}</td>
                                <td>{item.analytics.totalTrades}</td>
                                <td>{item.analytics.winRate}%</td>
                                <td>Rs.{item.analytics.totalProfitLoss}</td>
                                <td>{item.analytics.profitFactor}</td>
                            </tr>

                        ))}

                    </tbody>

                </table>

            </div>

        </main>

    )
}

export default Strategies