import { useEffect, useState } from 'react'

function Trades({ onSelectTrade }) {
    const [trades, setTrades] = useState([])
    const [file, setFile] = useState(null)

    const loadTrades = () => {
        fetch('http://localhost:8080/api/trades/broker-account/1', {
            credentials: 'include'
        })
            .then(response => response.json())
            .then(data => {
                console.log('Trades data:', data)
                setTrades(data)
            })
    }

    useEffect(() => {
        loadTrades()
    }, [])

    const handleFileChange = (e) => {
        setFile(e.target.files[0])
    }

    const handleUpload = async () => {
        if (!file) {
            return
        }

        const csrfResponse = await fetch(
            'http://localhost:8080/api/auth/csrf',
            {
                credentials: 'include'
            }
        )

        const csrfData = await csrfResponse.json()

        const formData = new FormData()

        formData.append('brokerAccountId', '1')
        formData.append('file', file)

        const response = await fetch(
            'http://localhost:8080/api/import/executions',
            {
                method: 'POST',
                credentials: 'include',
                headers: {
                    'X-XSRF-TOKEN': csrfData.token
                },
                body: formData
            }
        )

        console.log('Import status:', response.status)

        const responseText = await response.text()

        console.log('Import response:', responseText)

        if (response.ok) {
            loadTrades()
        }

    }

    return (
        <main>
            <h2>Trades</h2>

            <section>
                <h3>Import Executions</h3>

                <input
                    type="file"
                    accept=".csv"
                    onChange={handleFileChange}
                />

                <button onClick={handleUpload}>
                    Import CSV
                </button>
            </section>

            <section>
                <h3>Trade History</h3>

                {trades.length === 0 ? (
                    <p>No trades available.</p>
                ) : (
                    <table>
                        <thead>
                        <tr>
                            <th>Symbol</th>
                            <th>Quantity</th>
                            <th>Entry Price</th>
                            <th>Exit Price</th>
                            <th>P&L</th>
                            <th>Entry Time</th>
                            <th>Exit Time</th>
                        </tr>
                        </thead>

                        <tbody>
                        {trades.map((trade) => (
                            <tr
                                key={trade.id}
                                onClick={() => onSelectTrade(trade.id)}
                            >
                                <td>{trade.instrument.symbol}</td>
                                <td>{trade.quantity}</td>
                                <td>{trade.entryPrice}</td>
                                <td>{trade.exitPrice}</td>
                                <td>{trade.profitLoss}</td>
                                <td>{trade.entryTime}</td>
                                <td>{trade.exitTime}</td>
                            </tr>
                        ))}
                        </tbody>
                    </table>
                )}
            </section>
        </main>
    )
}

export default Trades