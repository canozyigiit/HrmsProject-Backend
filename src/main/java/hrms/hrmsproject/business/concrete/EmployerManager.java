package hrms.hrmsproject.business.concrete;


import hrms.hrmsproject.business.abstracts.EmployerService;
import hrms.hrmsproject.business.abstracts.ValidateService;
import hrms.hrmsproject.business.constants.Messages;
import hrms.hrmsproject.core.utilities.business.BusinessRules;
import hrms.hrmsproject.core.utilities.results.*;
import hrms.hrmsproject.core.utilities.uploads.FileService;
import hrms.hrmsproject.dataAccess.abstracts.EmployerDao;
import hrms.hrmsproject.entities.concretes.Employer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Service
public class EmployerManager implements EmployerService {
    private EmployerDao employerDao;
    private ValidateService<Employer> validateService;
    private FileService fileService;

    @Autowired
    public EmployerManager(EmployerDao employerDao,ValidateService<Employer> validateService, FileService fileService) {
        this.employerDao = employerDao;
        this.validateService = validateService;
        this.fileService = fileService;
    }

    @Override
    public Result add(Employer employer) {
        Result result = BusinessRules.Run(checkIfEmployerEmailExists(employer),
                checkEmailIsCompatibleWithDomain(employer.getEmail(),employer.getWebSite()));
        if (result != null) {
            return result;
        }
        this.employerDao.save(employer);

        return new SuccessResult(Messages.employerAdded);

    }
    @Override
    public Result validateEmployer(int id){
       return validateService.verifyData(this.employerDao.getById(id));
    }

    @Override
    public Result addImageEmployer(MultipartFile file, int employerId) {
        Map<String, String> uploader = (Map<String, String>)fileService.save(file).getData();
        String imageUrl= uploader.get("url");
        Employer employer = employerDao.getOne(employerId);
        employer.setPhoto(imageUrl);
        employerDao.save(employer);
        return new SuccessResult("Image added");
    }

    @Override
    public DataResult<List<Employer>> getAll() {
        return new SuccessDataResult<List<Employer>>(this.employerDao.findAll(), Messages.employerGetAll);
    }

    @Override
    public DataResult<Employer> getById(int id) {
        return new SuccessDataResult<Employer>(this.employerDao.findById(id).orElse(null), Messages.employerGet);
    }

    @Override
    public Result delete(Employer employer) {
        this.employerDao.delete(employer);
        return new SuccessResult(Messages.employerDeleted);

    }

    //************************************************************************************************************

    private Result checkIfEmployerEmailExists(Employer employer) {
        var result = employerDao.findAllByEmail(employer.getEmail()).stream().count() != 0;
        if (result) {
            return new ErrorResult(Messages.employerEmailExists);
        }
        return new SuccessResult();
    }//Böyle email daha önce kullanılmış mı ?


//    private Result checkIfEmployerEmail(String email, String webSite) {
//        Pattern validEmail =
//                Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$",
//                        Pattern.CASE_INSENSITIVE);
//
//        Matcher matcher = validEmail.matcher(email);
//
//        String[] isEmailCompatible = email.split("@", 2);//İkiye bölüyor  öncesi@sonrası
//        String webSiten = webSite.substring(4);//www. den sonrası
//
//        if (!matcher.matches() ) {
//            return new ErrorResult(Messages.errorEmployerEmailValid);
//        }else if (!isEmailCompatible[1].equals(webSiten)){
//            return new ErrorResult(Messages.errorEmployerEmailNotCorporate);
//        }
//
//        return new SuccessResult();
//    }
    private Result checkEmailIsCompatibleWithDomain(String email, String employerWebSite){

        String[] isEmailCompatible = email.split("@", 2);//İkiye bölüyor  öncesi@sonrası
        String webSite = employerWebSite.substring(4);//www. den sonrası

        if (!isEmailCompatible[1].equals(webSite)){
            return new ErrorResult(Messages.errorEmployerEmailNotCorporate);
        }

        return new SuccessResult();
    }//Kurumsal eposta mı?
}
