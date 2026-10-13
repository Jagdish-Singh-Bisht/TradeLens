import './Trades.css'
import { useEffect, useState } from 'react'

function Trades({ onSelectTrade }) {
    const [trades, setTrades] = useState([])
    const [file, setFile] = useState(null)

    const [candleFile, setCandleFile] = useState(null)
    const [candleMessage, setCandleMessage] = useState('')
    const [candleError, setCandleError] = useState('')
    const [importingCandles, setImportingCandles] = useState(false)

    const [search, setSearch] = useState('')
    const [resultFilter, setResultFilter] = useState('ALL')
    const [loading, setLoading] = useState(true)
    const [importing, setImporting] = useState(false)
    const [message, setMessage] = useState('')
    const [error, setError] = useState('')

    const loadTrades = async () => {
        setLoading(true)
        setError('')

        try {
            const response = await fetch(
                'http://localhost:8080/api/trades/broker-account/1',
                { credentials: 'include' }
            )

            if (!response.ok) {
                throw new Error('Failed to load trades.')
            }

            const data = await response.json()

            if (!Array.isArray(data)) {
                throw new Error('Unexpected response from server.')
            }

            setTrades(data)
        } catch (err) {
            setError(err.message || 'Unable to load trades.')
        } finally {
            setLoading(false)
        }
    }

    useEffect(() => {
        loadTrades()
    }, [])

    const handleFileChange = (event) => {
        setFile(event.target.files[0] || null)
        setMessage('')
        setError('')
    }

    const handleUpload = async () => {
        if (!file) {
            setError('Please select a CSV file first.')
            return
        }

        setImporting(true)
        setMessage('')
        setError('')

        try {
            const csrfResponse = await fetch(
                'http://localhost:8080/api/auth/csrf',
                { credentials: 'include' }
            )

            if (!csrfResponse.ok) {
                throw new Error('Unable to obtain CSRF token.')
            }

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

            const responseText = await response.text()

            if (!response.ok) {
                throw new Error(
                    responseText || 'CSV import failed.'
                )
            }

            setMessage('CSV imported successfully.')
            setFile(null)

            await loadTrades()
        } catch (err) {
            setError(err.message || 'Unable to import CSV.')
        } finally {
            setImporting(false)
        }
    }


    const handleCandleUpload = async () => {
        if (!candleFile) {
            setCandleError('Please select a candle CSV file first.')
            return
        }

        setImportingCandles(true)
        setCandleMessage('')
        setCandleError('')

        try {
            const csrfResponse = await fetch(
                'http://localhost:8080/api/auth/csrf',
                { credentials: 'include' }
            )

            if (!csrfResponse.ok) {
                throw new Error('Unable to obtain CSRF token.')
            }

            const csrfData = await csrfResponse.json()
            const formData = new FormData()

            formData.append('file', candleFile)

            const response = await fetch(
                'http://localhost:8080/api/import/market-candles',
                {
                    method: 'POST',
                    credentials: 'include',
                    headers: {
                        'X-XSRF-TOKEN': csrfData.token
                    },
                    body: formData
                }
            )

            const responseText = await response.text()

            if (!response.ok) {
                throw new Error(
                    responseText || 'Market candle import failed.'
                )
            }

            setCandleMessage(responseText || 'Market candles imported successfully.')
            setCandleFile(null)
        } catch (err) {
            setCandleError(err.message || 'Unable to import market candles.')
        } finally {
            setImportingCandles(false)
        }
    }


    const filteredTrades = trades.filter((trade) => {
        const symbol = trade.instrument?.symbol || ''

        const matchesSearch = symbol
            .toLowerCase()
            .includes(search.trim().toLowerCase())

        const profitLoss = Number(trade.profitLoss ?? 0)

        const matchesResult =
            resultFilter === 'ALL' ||
            (resultFilter === 'WIN' && profitLoss > 0) ||
            (resultFilter === 'LOSS' && profitLoss < 0) ||
            (resultFilter === 'BREAKEVEN' && profitLoss === 0)

        return matchesSearch && matchesResult
    })

    const formatPrice = (value) => {
        if (value == null || !Number.isFinite(Number(value))) {
            return '—'
        }

        return `₹${Number(value).toLocaleString('en-IN', {
            minimumFractionDigits: 2,
            maximumFractionDigits: 2
        })}`
    }

    const formatDate = (value) => {
        if (!value) return '—'

        const date = new Date(value)

        if (Number.isNaN(date.getTime())) {
            return value
        }

        return date.toLocaleString('en-IN')
    }

    return (
        <main className="trades-page">
            <header className="trades-heading">
                <h2>Trades</h2>
                <p>Review your executions, trade history and results.</p>
            </header>

            <section className="trades-import">
                <h3>Import Executions</h3>
                <p>Upload a CSV file containing your execution history.</p>

                <div className="trades-import-controls">
                    <input
                        type="file"
                        accept=".csv"
                        onChange={handleFileChange}
                    />

                    <button
                        onClick={handleUpload}
                        disabled={!file || importing}
                    >
                        {importing ? 'Importing...' : 'Import CSV'}
                    </button>
                </div>

                {file && (
                    <p>Selected file: {file.name}</p>
                )}

                {message && (
                    <p className="trades-success" role="status">
                        {message}
                    </p>
                )}

                {error && (
                    <p className="trades-error" role="alert">
                        {error}
                    </p>
                )}
            </section>


            <section className="trades-import">
                <h3>Import Market Candles</h3>
                <p>
                    Upload historical OHLCV candle data for your candlestick charts.
                </p>

                <div className="trades-import-controls">
                    <input
                        type="file"
                        accept=".csv"
                        onChange={(event) => {
                            setCandleFile(event.target.files[0] || null)
                            setCandleMessage('')
                            setCandleError('')
                        }}
                    />

                    <button
                        onClick={handleCandleUpload}
                        disabled={!candleFile || importingCandles}
                    >
                        {importingCandles ? 'Importing...' : 'Import Candles'}
                    </button>
                </div>

                {candleFile && (
                    <p>Selected file: {candleFile.name}</p>
                )}

                {candleMessage && (
                    <p className="trades-success" role="status">
                        {candleMessage}
                    </p>
                )}

                {candleError && (
                    <p className="trades-error" role="alert">
                        {candleError}
                    </p>
                )}
            </section>


            <section className="trades-history">
                <div className="trades-history-heading">
                    <div>
                        <h3>Trade History</h3>
                        <p>
                            {filteredTrades.length} of {trades.length} trades
                        </p>
                    </div>

                    <button onClick={loadTrades} disabled={loading}>
                        {loading ? 'Loading...' : 'Refresh'}
                    </button>
                </div>

                <div className="trades-filters">
                    <input
                        type="search"
                        placeholder="Search by symbol..."
                        value={search}
                        onChange={(event) => setSearch(event.target.value)}
                    />

                    <select
                        value={resultFilter}
                        onChange={(event) =>
                            setResultFilter(event.target.value)
                        }
                    >
                        <option value="ALL">All results</option>
                        <option value="WIN">Profitable</option>
                        <option value="LOSS">Loss-making</option>
                        <option value="BREAKEVEN">Breakeven</option>
                    </select>
                </div>

                {loading ? (
                    <p>Loading trades...</p>
                ) : error && trades.length === 0 ? (
                    <p>Trades could not be loaded. Check the error above.</p>
                ) : filteredTrades.length === 0 ? (
                    <p>No trades match your search or filter.</p>
                ) : (
                    <div className="trades-table-wrapper">
                        <table className="trades-table">
                            <thead>
                            <tr>
                                <th>Symbol</th>
                                <th>Quantity</th>
                                <th>Entry Price</th>
                                <th>Exit Price</th>
                                <th>P&amp;L</th>
                                <th>Entry Time</th>
                                <th>Exit Time</th>
                            </tr>
                            </thead>

                            <tbody>
                            {filteredTrades.map((trade) => {
                                const profitLoss = Number(
                                    trade.profitLoss ?? 0
                                )

                                return (
                                    <tr
                                        key={trade.id}
                                        onClick={() =>
                                            onSelectTrade(trade.id)
                                        }
                                        tabIndex={0}
                                        onKeyDown={(event) => {
                                            if (
                                                event.key === 'Enter' ||
                                                event.key === ' '
                                            ) {
                                                event.preventDefault()
                                                onSelectTrade(trade.id)
                                            }
                                        }}
                                    >
                                        <td>
                                            {trade.instrument?.symbol || '—'}
                                        </td>
                                        <td>{trade.quantity ?? '—'}</td>
                                        <td>{formatPrice(trade.entryPrice)}</td>
                                        <td>{formatPrice(trade.exitPrice)}</td>
                                        <td
                                            className={
                                                profitLoss > 0
                                                    ? 'profit-value'
                                                    : profitLoss < 0
                                                        ? 'loss-value'
                                                        : ''
                                            }
                                        >
                                            {formatPrice(trade.profitLoss)}
                                        </td>
                                        <td>{formatDate(trade.entryTime)}</td>
                                        <td>{formatDate(trade.exitTime)}</td>
                                    </tr>
                                )
                            })}
                            </tbody>
                        </table>
                    </div>
                )}
            </section>
        </main>
    )
}

export default Trades
