export const restrictToNumbers = (value) => value.replace(/\D/g, '');

export const formatName = (value) => value.replace(/[^a-zA-Z\s]/g, '');

export const formatPhone = (value) => {
    const numbers = restrictToNumbers(value);
    return numbers.substring(0, 10);
};

export const formatPincode = (value) => {
    const numbers = restrictToNumbers(value);
    return numbers.substring(0, 6);
};

export const getMaxDateFor18YearsOld = () => {
    const maxDate = new Date();
    maxDate.setFullYear(maxDate.getFullYear() - 18);
    return maxDate.toISOString().split('T')[0];
};
