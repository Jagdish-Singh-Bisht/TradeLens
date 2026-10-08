import './SummaryCard.css'


function SummaryCard({ title, value, type}) {

    return (
        <div className={`summary-card ${type || ''}`}>
            <h3>{title}</h3>
            <p>{value}</p>
        </div>
    )
}

export default SummaryCard