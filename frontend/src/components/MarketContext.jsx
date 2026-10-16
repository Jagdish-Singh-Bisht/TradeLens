import CandlestickChart from './CandlestickChart'
import './MarketContext.css'
import { useEffect, useState } from 'react'

function MarketContext() {
    const [marketContext, setMarketContext] = useState(null)
    const [error, setError] = useState('')


    useEffect(() => {
        async function loadMarketContext() {
            try {
                setError('');

                const accountsResponse = await fetch(
                    'http://localhost:8080/api/broker-accounts',
                    { credentials: 'include' }
                );

                if (!accountsResponse.ok) {
                    throw new Error(
                        `Failed to load accounts: HTTP ${accountsResponse.status}`
                    );
                }

                const accounts = await accountsResponse.json();

                if (!Array.isArray(accounts) || accounts.length === 0) {
                    setMarketContext([]);
                    setError(
                        'No broker account found. Create an account before viewing market context.'
                    );
                    return;
                }

                const brokerAccountId = accounts[0].id;

                const response = await fetch(
                    `http://localhost:8080/api/trades/market-context/${brokerAccountId}`,
                    { credentials: 'include' }
                );

                if (!response.ok) {
                    throw new Error(
                        `Failed to load market context: HTTP ${response.status}`
                    );
                }

                const data = await response.json();

                if (!Array.isArray(data)) {
                    throw new Error(
                        'Expected a list of market-context records.'
                    );
                }

                setMarketContext(data);

            } catch (error) {
                console.error('Error loading market context:', error);
                setError(error.message);
            }
        }

        loadMarketContext();
    }, []);


    if (error) {
        return (
            <main className="market-context">
                <div className="market-context-heading">
                    <h2>Market Context</h2>
                </div>
                <p>Could not load market context: {error}</p>
            </main>
        )
    }

    if (marketContext === null) {
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

            <CandlestickChart />

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
                            <td>
                                {trade.entryPrice != null ? `Rs. ${trade.entryPrice}` : '—'}
                            </td>
                            <td>
                                {trade.exitPrice != null ? `Rs. ${trade.exitPrice}` : '—'}
                            </td>
                            <td>
                                {trade.mfe != null ? `Rs. ${trade.mfe}` : '—'}
                            </td>
                            <td>
                                {trade.mae != null ? `Rs. ${trade.mae}` : '—'}
                            </td>
                            <td>
                                {trade.postExitMovement != null
                                    ? `Rs. ${trade.postExitMovement}`
                                    : '—'}
                            </td>

                        </tr>
                    ))}
                    </tbody>
                </table>
            </div>
        </main>
    )
}

export default MarketContext