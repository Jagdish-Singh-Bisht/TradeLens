
import {
    createContext,
    useContext,
    useState
} from 'react';

const DateRangeContext = createContext(null);

export function DateRangeProvider({ children }) {

    const [dateRange, setDateRange] = useState('LAST_30_DAYS');

    const [customStartDate, setCustomStartDate] = useState('');
    const [customEndDate, setCustomEndDate] = useState('');

    const getLocalDateString = (date) => {
        const year = date.getFullYear();
        const month = String(date.getMonth() + 1).padStart(2, '0');
        const day = String(date.getDate()).padStart(2, '0');

        return `${year}-${month}-${day}`;
    };

    const today = new Date();
    today.setHours(0, 0, 0, 0);

    let startDate = null;
    let endDate = null;

    if (dateRange === 'LAST_7_DAYS') {
        const start = new Date(today);
        start.setDate(today.getDate() - 6);

        startDate = getLocalDateString(start);
        endDate = getLocalDateString(today);

    } else if (dateRange === 'LAST_30_DAYS') {
        const start = new Date(today);
        start.setDate(today.getDate() - 29);

        startDate = getLocalDateString(start);
        endDate = getLocalDateString(today);

    } else if (dateRange === 'LAST_90_DAYS') {
        const start = new Date(today);
        start.setDate(today.getDate() - 89);

        startDate = getLocalDateString(start);
        endDate = getLocalDateString(today);

    } else if (dateRange === 'CUSTOM') {
        startDate = customStartDate || null;
        endDate = customEndDate || null;
    }

    const dateFilterError =
        dateRange === 'CUSTOM'
            ? !startDate || !endDate
                ? 'Please select both start and end dates.'
                : startDate > endDate
                    ? 'Start date cannot be after end date.'
                    : null
            : null;

    const contextValue = {
        dateRange,
        setDateRange,
        customStartDate,
        setCustomStartDate,
        customEndDate,
        setCustomEndDate,
        startDate,
        endDate,
        dateFilterError
    };

    return (
        <DateRangeContext.Provider value={contextValue}>
            {children}
        </DateRangeContext.Provider>
    );
}

export function useDateRange() {

    const context = useContext(DateRangeContext);

    if (!context) {
        throw new Error(
            'useDateRange must be used inside DateRangeProvider.'
        );
    }

    return context;
}
