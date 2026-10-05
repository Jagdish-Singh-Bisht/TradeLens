
function Sidebar({onNavigate}) {

    return (
        <aside>
            <h2>TradeLens</h2>

            <nav>

                <button onClick={() => onNavigate('dashboard')}>
                    Dashboard
                </button>

                <button onClick={() => onNavigate('trades')}>
                    Trades
                </button>

                <a href="#">Analytics</a>
                <a href="#">Strategies</a>
                <a href="#">Behavior</a>
                <a href="#">Market Context</a>
            </nav>

        </aside>
    )
}

export default Sidebar