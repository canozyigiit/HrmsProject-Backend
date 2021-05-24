package hrms.hrmsproject.business.concrete;

import hrms.hrmsproject.business.abstracts.JobSeekerService;
import hrms.hrmsproject.business.constans.Messages;
import hrms.hrmsproject.core.adapters.MernisService;
import hrms.hrmsproject.core.business.BusinessRules;
import hrms.hrmsproject.core.utilities.results.*;
import hrms.hrmsproject.dataAccess.abstracts.JobSeekerDao;
import hrms.hrmsproject.entities.concretes.JobSeeker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class JobSeekerManager implements JobSeekerService {
    private JobSeekerDao jobSeekerDao;
    private MernisService mernisService;

    @Autowired
    public JobSeekerManager(JobSeekerDao jobSeekerDao) {
        this.jobSeekerDao = jobSeekerDao;
    }

    @Override
    public Result add(JobSeeker jobSeeker) {
        Result result = BusinessRules.Run(checkIfJobSeekerEmailExists(jobSeeker), checkIfJobSeekerDomain(jobSeeker),
                jobSeekerEmailValid(jobSeeker.getEmail()));
        if (result != null) {
            return result;
        } else if (!mernisService.checkIfRealPerson(jobSeeker.getNationalityId(), jobSeeker.getFirstName(),
                jobSeeker.getLastName(), jobSeeker.getDateOfBirth())) {
            return new ErrorResult(Messages.notRealPerson);
        }
        this.jobSeekerDao.save(jobSeeker);
        return new SuccessResult(Messages.jobSekeerAdded);
    }

    @Override
    public DataResult<List<JobSeeker>> getAll() {
        return new SuccessDataResult<List<JobSeeker>>(this.jobSeekerDao.findAll(), Messages.jobSekeerGetAll);
    }

    @Override
    public DataResult<JobSeeker> getById(int id) {
        return new SuccessDataResult<JobSeeker>(this.jobSeekerDao.findById(id).orElse(null), Messages.jobSekeerGet);
    }

    @Override
    public Result delete(JobSeeker jobSeeker) {
        this.jobSeekerDao.delete(jobSeeker);
        return new SuccessResult(Messages.jobSekeerDeleted);
    }

    private Result jobSeekerEmailValid(String email) {
        Pattern validEmail =
                Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$",
                        Pattern.CASE_INSENSITIVE);

        Matcher matcher = validEmail.matcher(email);
        if (!matcher.matches()) {
            return new ErrorResult(Messages.errorjobSeekerEmail);
        }

        return new SuccessResult();
    }

    private Result checkIfJobSeekerEmailExists(JobSeeker jobSeeker) {
        var result = jobSeekerDao.findAllByEmail(jobSeeker.getEmail()).stream().count() != 0;
        if (result) {
            return new ErrorResult(Messages.jobSeekerEmailExists);
        }
        return new SuccessResult();
    }

    private Result checkIfJobSeekerDomain(JobSeeker jobSeeker) {
        if (jobSeeker.getEmail() == null && jobSeeker.getPassword() == null && jobSeeker.getFirstName() == null
                && jobSeeker.getLastName() == null && jobSeeker.getDateOfBirth() == null && jobSeeker.getNationalityId() == null) {
            return new ErrorResult(Messages.employerDomainCheck);
        }
        return new SuccessResult();
    }

//    private Result checkIfRealPerson(JobSeeker jobSeeker) {
//        if (!mernisService.checkIfRealPerson(jobSeeker.getNationalityId(), jobSeeker.getFirstName(),
//                jobSeeker.getLastName(), jobSeeker.getDateOfBirth())) ;
//        {
//            return new ErrorResult(Messages.notRealPerson);
//        }
//        return new SuccessResult();
//    }
}
