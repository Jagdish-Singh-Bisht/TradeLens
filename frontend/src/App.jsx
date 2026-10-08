import MarketContext from './components/MarketContext'
import Behavior from "./components/Behavior.jsx";
import Strategies from "./components/Strategies";
import Analytics from './components/Analytics'
import TradeDetails from './components/TradeDetails'
import Trades from './components/Trades'
// import Header from './components/Header'
import Sidebar from './components/Sidebar'
import Dashboard from './components/Dashboard'
import Login from './components/Login'
import './App.css'


import {useEffect, useState} from "react";

function App() {

    const [isLoggedIn, setIsLoggedIn] = useState(null)
    const [page, setPage] = useState('dashboard')
    const [selectedTradeId, setSelectedTradeId] = useState(null)

    useEffect(() => {
        fetch('http://localhost:8080/api/auth/me',
            {
                credentials: 'include'
            })
            .then(response => {
                if(response.ok) {
                    setIsLoggedIn(true)
                } else {
                    setIsLoggedIn(false)
                }
            })
            .catch(() => {
                setIsLoggedIn(false)
            })
    }, [])


    if(!isLoggedIn === null) {
        return <p>Checking Login...</p>
    }

    if(!isLoggedIn) {
        return <Login onLogin={() => setIsLoggedIn(true)} />
    }

  return (
      <div className="app">

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