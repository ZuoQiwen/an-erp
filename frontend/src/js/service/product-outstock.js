import { http } from '../libs/http';

var search = (queryParameters) => http.post('product-outstock/search', queryParameters);

var add = (productoutstock) => http.post('product-outstock/add', productoutstock);

var getById = (billId) => http.post('product-outstock/getById', { billId: billId });

var remove = (idList) => http.post('product-outstock/remove', { idList: idList });

var update = (productoutstock) => http.post('product-outstock/update', productoutstock);


export default { add, getById, remove, search, update };
