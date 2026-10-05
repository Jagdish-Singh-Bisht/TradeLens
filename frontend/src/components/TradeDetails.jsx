import './TradeDetails.css'

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

    return (

        <main>
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
                        <strong>Rs.{trade.entryPrice}</strong>
                    </div>

                    <div className="trade-stat">
                        <span>Exit Price</span>
                        <strong>Rs.{trade.exitPrice}</strong>
                    </div>

                    <div className="trade-stat">
                        <span>P&L</span>
                        <strong>Rs.{trade.profitLoss}</strong>
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
                                <strong>Rs.{trade.decision.plannedEntry}</strong>
                            </div>

                            <div className="decision-item">
                                <span>Target</span>
                                <strong>Rs.{trade.decision.target}</strong>
                            </div>

                            <div className="decision-item">
                                <span>Stop Loss</span>
                                <strong>Rs.{trade.decision.stopLoss}</strong>
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

                    <form onSubmit={handleSubmit}>

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

            <section className="analysis-section">
                <div className="section-header">
                    <h3>Decision vs Actual</h3>
                    <span>Plan vs execution</span>
                </div>

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
                                    <strong>Rs.{decisionAnalysis.plannedEntry}</strong>
                                </div>

                                <div>
                                    <span>Actual Entry</span>
                                    <strong>Rs.{decisionAnalysis.actualEntry}</strong>
                                </div>

                                <div>
                                    <span>Deviation</span>
                                    <strong>Rs.{decisionAnalysis.entryDeviation}</strong>
                                </div>

                            </div>

                        </div>

                        <div className="analysis-group">
                            <h4>Exit</h4>

                            <div className="analysis-grid">
                                <div>
                                    <span>Target</span>
                                    <strong>Rs.{decisionAnalysis.target}</strong>
                                </div>

                                <div>
                                    <span>Actual Exit</span>
                                    <strong>Rs.{decisionAnalysis.actualExit}</strong>
                                </div>

                                <div>
                                    <span>Deviation</span>
                                    <strong>Rs.{decisionAnalysis.exitDeviation}</strong>
                                </div>

                            </div>

                        </div>

                        <div className="analysis-group">
                            <h4>Risk & Result</h4>

                            <div className="analysis-grid">
                                <div>
                                    <span>Stop Loss</span>
                                    <strong>Rs.{decisionAnalysis.stopLoss}</strong>
                                </div>

                                <div>
                                    <span>Planned Risk</span>
                                    <strong>Rs.{decisionAnalysis.plannedRisk}</strong>
                                </div>

                                <div>
                                    <span>Actual P&L</span>
                                    <strong>Rs.{decisionAnalysis.actualProfitLoss}</strong>
                                </div>
                            </div>

                            <div className="analysis-status">
                                <span>
                                    Target Achieved:
                                    <strong>{decisionAnalysis.targetAchieved ? ' Yes' : ' No'}</strong>
                                </span>

                                <span>
                                    Stop Loss Hit:
                                    <strong>{decisionAnalysis.stopLossHit ? ' Yes' : ' No'}</strong>
                                </span>
                            </div>

                        </div>

                    </div>

                )}

            </section>

        </main>
    )
}

export default TradeDetails