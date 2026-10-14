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

import {
    BrowserRouter,
    Routes,
    Route,
    Navigate,
    useNavigate,
    useLocation,
    useParams,
} from 'react-router-dom'

function TradeDetailsPage() {
    const {tradeId} = useParams()

    return <TradeDetails tradeId={tradeId} />
}

function AppLayout() {

    const navigate = useNavigate();
    const location = useLocation();

    const currentPage = location.pathname.startsWith('/trades/')
        ? 'trade-details'
        : location.pathname.slice(1) || 'dashboard'

    return (

        <div className="app">
            <div className="app-body">

                <Sidebar
                    onNavigate={(nextPage) => navigate(`/${nextPage}`)}
                    currentPage={currentPage}
                />

                <Routes>
                    <Route
                        path="/"
                        element={<Navigate to="/dashboard" replace />}
                    />

                    <Route
                        path="/dashboard"
                        element={<Dashboard />}
                    />

                    <Route
                        path="/trades"
                        element={
                            <Trades
                                onSelectTrade={(tradeId) => {
                                    navigate(`/trades/${tradeId}`)
                                }}
                            />
                        }
                    />

                    <Route
                        path="/trades/:tradeId"
                        element={<TradeDetailsPage/>}
                    />


                    <Route
                        path="/analytics"
                        element={<Analytics />}
                    />

                    <Route
                        path="/strategies"
                        element={<Strategies />}
                    />

                    <Route
                        path="/market-context"
                        element={<MarketContext />}
                    />

                    <Route
                        path="*"
                        element={<Navigate to="/dashboard" replace />}
                    />

                </Routes>

            </div>
        </div>
    )

}

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
                setIsLoggedIn(response.ok)
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
      <BrowserRouter>
          <AppLayout />
      </BrowserRouter>
  )

}

export default App