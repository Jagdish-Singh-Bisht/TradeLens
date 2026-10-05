import './SummaryCard.css'


function SummaryCard({ title, value}) {

    return (
        <div>
            <h3>{title}</h3>
            <p>{value}</p>
        </div>
    )
}

export default SummaryCard