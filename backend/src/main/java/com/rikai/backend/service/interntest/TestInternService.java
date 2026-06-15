package com.rikai.backend.service.interntest;

import com.rikai.backend.common.InternStatus;
import com.rikai.backend.dto.request.agent_ai.GeneratedTestDto;
import com.rikai.backend.model.*;
import com.rikai.backend.model.Enum.QuestionType;
import com.rikai.backend.repository.*;
import com.rikai.backend.service.ai.AiGenericService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class TestInternService {
    private final AnswerOptionRepository answerOptionRepository;
    private final QuestionRepository questionRepository;
    private final AiGenericService aiGenericService;
    private final InternRepository internRepository;
    private final TestRepository testRepository;
    private final TestAssignmentRepository testAssignmentRepository;

    public void autoCreateInternTests() {
        List<Intern> interns = this.internRepository.findAllByInternStatus(InternStatus.ACTIVE);
        if (interns.isEmpty()) return;

        Map<Long, Set<Integer>> existingEvalMap = testAssignmentRepository
                .findInternIdAndEvaluationNumberByInternIn(interns)
                .stream()
                .collect(Collectors.groupingBy(
                        row -> (Long) row[0],
                        Collectors.mapping(row -> (Integer) row[1], Collectors.toSet())
                ));

        LocalDate today = LocalDate.now();

        for (Intern intern : interns) {
            long days = ChronoUnit.DAYS.between(intern.getStartDate(), today);
            int expectedEvaluation = (int) (days / 14);

            Set<Integer> existingEvals = existingEvalMap.getOrDefault(intern.getId(), Collections.emptySet());

            for (int i = 1; i <= expectedEvaluation; i++) {
                if (!existingEvals.contains(i)) {
                    this.createTestIntern(intern, i);
                }
            }
        }
    }

    @Transactional
    public void createTestIntern(Intern intern, int evaluationNumber) {
        String topic = intern.getPosition().getTitle();
        GeneratedTestDto testDto = this.aiGenericService.generatedTest(5, topic, evaluationNumber);

        Test test = Test.builder()
                .title(testDto.getTitle())
                .build();
        this.testRepository.save(test);

        List<Question> questions = testDto.getQuestions().stream()
                .map(q -> Question.builder()
                        .type(QuestionType.SINGLE_CHOICE)
                        .content(q.getContent())
                        .build())
                .toList();

        List<Question> savedQuestions = questionRepository.saveAll(questions);

        List<AnswerOption> answerOptions = new ArrayList<>();
        for (int i = 0; i < testDto.getQuestions().size(); i++) {
            GeneratedTestDto.QuestionDto questionDto = testDto.getQuestions().get(i);
            Question savedQuestion = savedQuestions.get(i);

            for (GeneratedTestDto.OptionDto optionDto : questionDto.getOptions()) {
                answerOptions.add(AnswerOption.builder()
                        .content(optionDto.getContent())
                        .question(savedQuestion)
                        .isCorrect(optionDto.isCorrect())
                        .build());
            }
        }

        answerOptionRepository.saveAll(answerOptions);


         testAssignmentRepository.save(TestAssignment.builder()
                 .intern(intern)
                 .test(test)
                 .evaluationNumber(evaluationNumber)
                 .build());
    }
}












//@Service
//@RequiredArgsConstructor
//public class TestInternService {
//    private final AnswerOptionRepository answerOptionRepository;
//    private final QuestionRepository questionRepository;
//    private final AiGenericService aiGenericService;
//    private final InternRepository internRepository;
//    private final TestRepository testRepository;
//    private final TestAssignmentRepository testAssignmentRepository;
//        public void autoCreateInternTests(){
//            List<Intern> interns = this.internRepository.findAllByInternStatus(InternStatus.ACTIVE);
//            for(Intern intern : interns){
//                long days = ChronoUnit.DAYS.between(
//                        intern.getStartDate(),
//                        LocalDate.now()
//                );
//                int expectedEvaluation = (int) (days/14);
//
//
//                for(int i=1;i<=expectedEvaluation;i++){
//                    if(!this.testAssignmentRepository.existsByInternAndEvaluationNumber(intern,i)){
//                        this.createTestIntern(intern,i);
//                    }
//                }
//            }
//
//        }
//
//        public void createTestIntern(Intern intern,int  evaluationNumber){
//            String topic = intern.getPosition().getTitle();
//            GeneratedTestDto testDto = this.aiGenericService.generatedTest(
//                    5,topic,evaluationNumber
//            );
//
//            Test test = Test.builder()
//                    .title(testDto.getTitle())
//                    .build();
//            this.testRepository.save(test);
//
//            for(GeneratedTestDto.QuestionDto questionDto  : testDto.getQuestions()){
//                Question question = Question.builder()
//                        .type(QuestionType.SINGLE_CHOICE)
//                        .content(questionDto.getContent())
//                        .build();
//                this.questionRepository.save(question);
//
//
//                for (GeneratedTestDto.OptionDto optionDto : questionDto.getOptions()){
//                    AnswerOption answerOption = AnswerOption.builder()
//                            .content(optionDto.getContent())
//                            .question(question)
//                            .isCorrect(optionDto.isCorrect())
//                            .build();
//                    this.answerOptionRepository.save(answerOption);
//
//                }
//
//            }
//
//
//        }
//
//}
