
function Sidebar({onNavigate, currentPage}) {

    return (
        <aside className="sidebar">

            <div className="sidebar-brand">
                <h2>TradeLens</h2>
                <span>Trading Analytics</span>
            </div>

            <nav className="sidebar-nav">

                <button
                    className={currentPage === 'dashboard' ? 'active' : ''}
                    onClick={() => onNavigate('dashboard')} >
                    Dashboard
                </button>

                <button
                    className={currentPage === 'trades' ? 'active' : ''}
                    onClick={() => onNavigate('trades')} >
                    Trades
                </button>

                <button
                    className={currentPage === 'analytics' ? 'active' : ''}
                    onClick={() => onNavigate('analytics')} >
                    Analytics
                </button>

                <button
                    className={currentPage === 'strategies' ? 'active' : ''}
                    onClick={() => onNavigate('strategies')} >
                    Strategies
                </button>

                <button
                    className={currentPage === 'behavior' ? 'active' : ''}
                    onClick={() => onNavigate('behavior')} >
                    Behavior
                </button>

                <button
                    className={currentPage === 'market-context' ? 'active' : ''}
                    onClick={() => onNavigate('market-context')} >
                    Market Context
                </button>

            </nav>

        </aside>
    )
}

export default Sidebar