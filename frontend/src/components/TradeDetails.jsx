import './TradeDetails.css'
import CandlestickChart from './CandlestickChart'
import { useEffect, useState } from 'react'



function TradeDetails({ tradeId }) {

    const [trade, setTrade] = useState(null)
    const [decisionAnalysis, setDecisionAnalysis] = useState(null)
    const [mfe, setMfe] = useState(null)
    const [mae, setMae] = useState(null)
    const [postExitMovement, setPostExitMovement] = useState(null)

    useEffect(() => {
        loadTrade()

        fetch(`http://localhost:8080/api/trades/${tradeId}/decision-analysis`,
            {
                credentials: 'include'
            })
            .then(response => response.json())
            .then(data => {
                console.log('Decision analysis: ', data)
                setDecisionAnalysis(data)
            })

        fetch(`http://localhost:8080/api/trades/${tradeId}/mfe`,
            {
                credentials: 'include'
            })
            .then(response => response.json())
            .then(data => {
                console.log('MFE: ', data)
                setMfe(data)
            })

        fetch(`http://localhost:8080/api/trades/${tradeId}/mae`,
            {
                credentials: 'include'
            })
            .then(response => response.json())
            .then(data => {
                console.log('MAE: ', data)
                setMae(data)
            })

        fetch(`http://localhost:8080/api/trades/${tradeId}/post-exit-movement`,
            {
                credentials: 'include'
            })
            .then(response => response.json())
            .then(data => {
                console.log('Post-exit movement: ', data)
                setPostExitMovement(data)
            })

    }, [tradeId])

    const [form, setForm] = useState({
        strategy: '',
        plannedEntry: '',
        target: '',
        stopLoss: '',
        confidence: '',
        entryReason: '',
        exitReason: ''
    })

    const loadTrade = () => {
        fetch(`http://localhost:8080/api/trades/${tradeId}`,
            {
                credentials: 'include'
            })
            .then(response => response.json())
            .then(data => {
                console.log('Trade details:', data)
                setTrade(data)
            })
    }

    const handleChange = (e) => {
        setForm({
            ...form,
            [e.target.name]: e.target.value
        })
    }

    const handleSubmit = async (e) => {
        e.preventDefault()

        const csrfResponse = await fetch(
            'http://localhost:8080/api/auth/csrf',
            {
                credentials: 'include'
            }
        )

        const csrfData = await csrfResponse.json()

        const response = await fetch(
            `http://localhost:8080/api/trades/${tradeId}/decision`,
            {
                method: 'POST',
                credentials: 'include',
                headers: {
                    'X-XSRF-TOKEN': csrfData.token,
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    strategy: form.strategy,
                    plannedEntry: Number(form.plannedEntry),
                    target: Number(form.target),
                    stopLoss: Number(form.stopLoss),
                    confidence: Number(form.confidence),
                    entryReason: form.entryReason,
                    exitReason: form.exitReason
                })
            }
        )

        console.log('Decision save status: ', response.status)

        if(response.ok) {
            setForm({
                strategy: '',
                plannedEntry: '',
                target: '',
                stopLoss: '',
                confidence: '',
                entryReason: '',
                exitReason: ''
            })

            loadTrade()
        } else {
            const error = await response.text()
            console.log('Decision save error: ', error)
        }
    }

    if(!trade) {
        return <main>Loading Trade...</main>
    }

    const formatAmount = (value) => {
        if (value == null || value === '') {
            return '—'
        }

        return `₹${Number(value).toLocaleString('en-IN', {
            minimumFractionDigits: 2,
            maximumFractionDigits: 2
        })}`
    }

    return (

        <main className="trade-details-page">
            <h2>Trade Details</h2>

            <section className="trade-summary">
                <div className="trade-title">
                    <div>
                        <h3>{trade.instrument.symbol}</h3>
                        <span>{trade.instrument.exchange}</span>
                    </div>
                </div>

                <div className="trade-summary-grid">

                    <div className="trade-stat">
                        <span>Quantity</span>
                        <strong>{trade.quantity}</strong>
                    </div>

                    <div className="trade-stat">
                        <span>Entry Price</span>
                        <strong>₹{trade.entryPrice}</strong>
                    </div>

                    <div className="trade-stat">
                        <span>Exit Price</span>
                        <strong>₹{trade.exitPrice}</strong>
                    </div>

                    <div className="trade-stat">
                        <span>P&L</span>
                        <strong>₹{trade.profitLoss}</strong>
                    </div>

                </div>

                <div className="trade-time">

                    <div>
                        <span>Entry Time</span>
                        <strong>{trade.entryTime}</strong>
                    </div>

                    <div>
                        <span>Exit Time</span>
                        <strong>{trade.exitTime}</strong>
                    </div>

                </div>

            </section>

            <section className="decision-section">
                <div className="section-header">
                    <h3>Trading Decision</h3>
                    <span>Pre-trade plan</span>
                </div>

                {trade.decision ? (
                    <div className="decision-card">

                        <div className="decision-grid">

                            <div className="decision-item">
                                <span>Strategy</span>
                                <strong>{trade.decision.strategy}</strong>
                            </div>

                            <div className="decision-item">
                                <span>Confidence</span>
                                <strong>{trade.decision.confidence}/10</strong>
                            </div>

                            <div className="decision-item">
                                <span>Planned Entry</span>
                                <strong>₹{trade.decision.plannedEntry}</strong>
                            </div>

                            <div className="decision-item">
                                <span>Target</span>
                                <strong>₹{trade.decision.target}</strong>
                            </div>

                            <div className="decision-item">
                                <span>Stop Loss</span>
                                <strong>₹{trade.decision.stopLoss}</strong>
                            </div>

                        </div>

                        <div className="decision-reasons">

                            <div>
                                <span>Entry Reason</span>
                                <p>{trade.decision.entryReason}</p>
                            </div>

                            <div>
                                <span>Exit Reason</span>
                                <p>{trade.decision.exitReason || 'Not provided'}</p>
                            </div>

                        </div>

                    </div>
                ) : (

                    <form className="trade-decision-form" onSubmit={handleSubmit}>

                        <div>
                            <label>Strategy</label>
                            <input
                                name="strategy"
                                value={form.strategy}
                                onChange={handleChange}
                                placeholder="e.g. Breakout"
                            />
                        </div>

                        <div>
                            <label>Planned Entry</label>
                            <input
                                type="number"
                                name="plannedEntry"
                                value={form.plannedEntry}
                                onChange={handleChange}
                            />
                        </div>

                        <div>
                            <label>Target</label>
                            <input
                                type="number"
                                name="target"
                                value={form.target}
                                onChange={handleChange}
                            />
                        </div>

                        <div>
                            <label>Stop Loss</label>
                            <input
                                type="number"
                                name="stopLoss"
                                value={form.stopLoss}
                                onChange={handleChange}
                            />
                        </div>

                        <div>
                            <label>Confidence</label>
                            <input
                                type="number"
                                min="1"
                                max="10"
                                name="confidence"
                                value={form.confidence}
                                onChange={handleChange}
                            />
                        </div>

                        <div>
                            <label>Entry Reason</label>
                            <input
                                name="entryReason"
                                value={form.entryReason}
                                onChange={handleChange}
                                placeholder="Why did you enter?"
                            />
                        </div>

                        <div>
                            <label>Exit Reason</label>
                            <input
                                name="exitReason"
                                value={form.exitReason}
                                onChange={handleChange}
                                placeholder="Why did you exit?"
                            />
                        </div>

                        <button type="submit">
                            Save Decision
                        </button>

                    </form>

                )}
            </section>

            <details className="analysis-section collapsible-section">
                <summary className="collapsible-heading">
                    <span>Decision vs Actual</span>
                    <span className="collapsible-hint">Plan vs execution</span>
                </summary>

                {!decisionAnalysis ? (
                    <div className="loading-card">
                        <p>Loading decision analysis...</p>
                    </div>
                ) : (
                    <div className="analysis-card">

                        <div className="analysis-group">
                            <h4>Entry</h4>

                            <div className="analysis-grid">
                                <div>
                                    <span>Planned Entry</span>
                                    <strong>{formatAmount(decisionAnalysis.plannedEntry)}</strong>
                                </div>

                                <div>
                                    <span>Actual Entry</span>
                                    <strong>{formatAmount(decisionAnalysis.actualEntry)}</strong>
                                </div>

                                <div>
                                    <span>Deviation</span>
                                    <strong>{formatAmount(decisionAnalysis.entryDeviation)}</strong>
                                </div>

                            </div>

                        </div>

                        <div className="analysis-group">
                            <h4>Exit</h4>

                            <div className="analysis-grid">
                                <div>
                                    <span>Target</span>
                                    <strong>{formatAmount(decisionAnalysis.target)}</strong>
                                </div>

                                <div>
                                    <span>Actual Exit</span>
                                    <strong>{formatAmount(decisionAnalysis.actualExit)}</strong>
                                </div>

                                <div>
                                    <span>Deviation</span>
                                    <strong>{formatAmount(decisionAnalysis.exitDeviation)}</strong>
                                </div>

                            </div>

                        </div>

                        <div className="analysis-group">
                            <h4>Risk & Result</h4>

                            <div className="analysis-grid">
                                <div>
                                    <span>Stop Loss</span>
                                    <strong>{formatAmount(decisionAnalysis.stopLoss)}</strong>
                                </div>

                                <div>
                                    <span>Planned Risk</span>
                                    <strong>{formatAmount(decisionAnalysis.plannedRisk)}</strong>
                                </div>

                                <div>
                                    <span>Actual P&amp;L</span>
                                    <strong
                                        className={
                                            Number(decisionAnalysis.actualProfitLoss) > 0
                                                ? 'analysis-profit'
                                                : Number(decisionAnalysis.actualProfitLoss) < 0
                                                    ? 'analysis-loss'
                                                    : ''
                                        }
                                    >
                                        {formatAmount(decisionAnalysis.actualProfitLoss)}
                                    </strong>
                                </div>
                            </div>

                            <div className="analysis-status">
                                <span>
                                    Target Achieved:
                                    <strong
                                        className={
                                            decisionAnalysis.targetAchieved
                                                ? 'analysis-profit'
                                                : 'analysis-neutral'
                                        }
                                    >
                                        {decisionAnalysis.targetAchieved ? ' Yes' : ' No'}
                                    </strong>
                                </span>

                                <span>
                                    Stop Loss Hit:
                                    <strong
                                        className={
                                            decisionAnalysis.stopLossHit
                                                ? 'analysis-loss'
                                                : 'analysis-neutral'
                                        }
                                    >
                                        {decisionAnalysis.stopLossHit ? ' Yes' : ' No'}
                                    </strong>
                                </span>

                            </div>

                        </div>

                    </div>

                )}

            </details>

            <details className="market-section collapsible-section">
                <summary className="collapsible-heading">
                    <span>Market Context</span>
                    <span className="collapsible-hint">Price movement during and after trade</span>
                </summary>

                {!mfe || !mae || !postExitMovement ? (
                    <div className="loading-card">
                        <p>Loading market context...</p>
                    </div>
                ) : (

                    <div className="market-card">

                        <div className="market-chart-group">
                            <h4>Historical Price Movement</h4>

                            <CandlestickChart
                                symbol={trade.instrument?.symbol}
                                exchange={trade.instrument?.exchange}
                                entryTime={trade.entryTime}
                                exitTime={trade.exitTime}
                                entryPrice={trade.entryPrice}
                                exitPrice={trade.exitPrice}
                            />

                        </div>

                        <div className="market-group">
                            <h4>During Trade</h4>

                            <div className="market-grid">

                                <div>
                                    <span>MFE</span>
                                    <strong>₹{mfe.mfe}</strong>
                                    <small>Maximum favorable movement</small>
                                </div>

                                <div>
                                    <span>Highest Price</span>
                                    <strong>₹{mfe.highestPrice}</strong>
                                    <small>Highest price during trade</small>
                                </div>

                                <div>
                                    <span>MAE</span>
                                    <strong>₹{mae.mae}</strong>
                                    <small>Maximum adverse movement</small>
                                </div>

                                <div>
                                    <span>Lowest Price</span>
                                    <strong>₹{mae.lowestPrice}</strong>
                                    <small>Lowest price during trade</small>
                                </div>

                            </div>

                        </div>

                        <div className="market-group">
                            <h4>After Exit</h4>

                            <div className="market-grid">

                                <div>
                                    <span>Post-exit Movement</span>
                                    <strong>₹{postExitMovement.postExitMovement}</strong>
                                    <small>Additional favorable movement</small>
                                </div>

                                <div>
                                    <span>Highest Price After Exit</span>
                                    <strong>₹{postExitMovement.highestPriceAfterExit}</strong>
                                    <small>Highest price after exit</small>
                                </div>

                            </div>

                        </div>

                    </div>
                )}


            </details>

        </main>
    )
}

export default TradeDetails