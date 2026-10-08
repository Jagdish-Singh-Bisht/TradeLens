import './MarketContext.css'
import { useEffect, useState } from 'react'

function MarketContext() {
    const [marketContext, setMarketContext] = useState(null)

    useEffect(() => {
        fetch('http://localhost:8080/api/trades/market-context/1', {
            credentials: 'include'
        })
            .then(response => response.json())
            .then(data => setMarketContext(data))
            .catch(error => console.error('Error loading market context:', error))
    }, [])

    if (!marketContext) {
        return (

            <main className="market-context">
                <div className="market-context-heading">
                    <h2>Market Context</h2>
                </div>
                <p>Loading market context...</p>
            </main>

        )
    }

    return (
        <main className="market-context">

            <div className="market-context-heading">
                <h2>Market Context</h2>
                <p>Understand price movement during and after your trades.</p>
            </div>

            <div className="market-context-intro">
                <p>
                    Review how price moved during and after each trade.
                </p>
            </div>

            <div className="market-context-table-container">
                <table className="market-context-table">
                    <thead>
                        <tr>
                            <th>Symbol</th>
                            <th>Entry Price</th>
                            <th>Exit Price</th>
                            <th>MFE</th>
                            <th>MAE</th>
                            <th>Post-Exit-Movement</th>
                        </tr>
                    </thead>

                    <tbody>
                        {marketContext.map((trade) => (
                            <tr key={trade.tradeId}>
                                <td>{trade.symbol}</td>
                                <td>Rs.{trade.entryPrice}</td>
                                <td>Rs.{trade.exitPrice}</td>
                                <td>Rs.{trade.mfe}</td>
                                <td>Rs.{trade.mae}</td>
                                <td>Rs.{trade.postExitMovement}</td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            </div>
        </main>
    )
}

export default MarketContext