import MarketContext from './components/MarketContext'
import Behavior from "./components/Behavior.jsx";
import Strategies from "./components/Strategies";
import Analytics from './components/Analytics'
import TradeDetails from './components/TradeDetails'
import Trades from './components/Trades'
import Header from './components/Header'
import Sidebar from './components/Sidebar'
import Dashboard from './components/Dashboard'
import Login from './components/Login'
import './App.css'


import {useState} from "react";

function App() {

    const [isLoggedIn, setIsLoggedIn] = useState(false)
    const [page, setPage] = useState('dashboard')
    const [selectedTradeId, setSelectedTradeId] = useState(null)


    if(!isLoggedIn) {
        return <Login onLogin={() => setIsLoggedIn(true)} />
    }

  return (
      <div className="app">
          <Header title="TradeLens Dashboard"/>

          <div className="app-body">

              <Sidebar
                  onNavigate={setPage}
                  currentPage={page}
              />

              { page === 'dashboard' && <Dashboard/> }

              { page === 'trades' && (
                  <Trades
                      onSelectTrade={(tradeId) => {
                        setSelectedTradeId(tradeId)
                        setPage('trade-details')
                      }}
                  />
              )}

              {page === 'trade-details' && (
                  <TradeDetails tradeId={selectedTradeId} />
              )}

              { page === 'analytics' && <Analytics /> }

              {page === 'strategies' && <Strategies />}

              {page === 'behavior' && <Behavior />}

              {page === 'market-context' && <MarketContext />}

          </div>

      </div>

  )

}

export default App